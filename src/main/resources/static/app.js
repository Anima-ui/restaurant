const { useEffect, useMemo, useState } = React;

const api = {
  async request(url, options = {}) {
    const response = await fetch(url, {
      headers: { "Content-Type": "application/json", ...(options.headers || {}) },
      ...options
    });

    if (!response.ok) {
      let errorPayload = null;
      try {
        errorPayload = await response.json();
      } catch (error) {
        errorPayload = { message: response.statusText };
      }
      const messageFromFields = Array.isArray(errorPayload?.fieldErrors) && errorPayload.fieldErrors.length > 0
        ? errorPayload.fieldErrors.map((fieldError) => fieldError.message).join(" ")
        : null;
      const requestError = new Error(messageFromFields || errorPayload?.message || `${response.status} ${response.statusText}`);
      requestError.fieldErrors = errorPayload?.fieldErrors || [];
      throw requestError;
    }

    if (response.status === 204) {
      return null;
    }

    return response.json();
  },
  getRestaurants() {
    return this.request("/api/v1/restaurants/all");
  },
  createRestaurant(payload) {
    return this.request("/api/v1/restaurants", {
      method: "POST",
      body: JSON.stringify(payload)
    });
  },
  updateRestaurant(id, payload) {
    return this.request(`/api/v1/restaurants/${id}`, {
      method: "PUT",
      body: JSON.stringify(payload)
    });
  },
  deleteRestaurant(id) {
    return this.request(`/api/v1/restaurants/${id}`, { method: "DELETE" });
  },
  getCustomers() {
    return this.request("/api/v1/customers");
  },
  createCustomer(payload) {
    return this.request("/api/v1/customers", {
      method: "POST",
      body: JSON.stringify(payload)
    });
  },
  deleteCustomer(id) {
    return this.request(`/api/v1/customers/${id}`, { method: "DELETE" });
  },
  startCustomerImport(payload) {
    return this.request("/api/v1/customers/bulk/async", {
      method: "POST",
      body: JSON.stringify(payload)
    });
  },
  getImportStatus(taskId) {
    return this.request(`/api/v1/customers/bulk/tasks/${taskId}`);
  },
  getBookings() {
    return this.request("/api/v1/bookings");
  },
  createBooking(payload) {
    return this.request("/api/v1/bookings", {
      method: "POST",
      body: JSON.stringify(payload)
    });
  },
  updateBookingStatus(id, status) {
    return this.request(`/api/v1/bookings/${id}/status`, {
      method: "PUT",
      body: JSON.stringify({ status })
    });
  }
};

function parseLines(text, mapper) {
  return text
    .split("\n")
    .map((line) => line.trim())
    .filter(Boolean)
    .map(mapper);
}

function parseRestaurantPayload(form) {
  const normalizedTables = form.tables.map((table) => ({
    tableNumber: Number(table.tableNumber),
    seats: Number(table.seats)
  }));
  const normalizedDishes = form.dishes.map((dish) => ({
    name: dish.name.trim(),
    price: Number(dish.price)
  }));
  const tableNumbers = normalizedTables.map((table) => table.tableNumber);
  const dishNames = normalizedDishes.map((dish) => dish.name.toLowerCase());
  const hasDuplicateTables = new Set(tableNumbers).size !== tableNumbers.length;
  const hasDuplicateDishes = new Set(dishNames).size !== dishNames.length;

  if (hasDuplicateTables) {
    throw new Error("Номера столов не должны повторяться.");
  }
  if (hasDuplicateDishes) {
    throw new Error("Названия блюд не должны повторяться.");
  }

  return {
    name: form.name.trim(),
    city: form.city.trim(),
    cuisineType: form.cuisineType.trim(),
    amenities: form.amenitiesText
      .split(",")
      .map((v) => v.trim())
      .filter(Boolean),

    tables: normalizedTables,

    dishes: normalizedDishes
  };
}

function parseBulkCustomers(text) {
  return parseLines(text, (line) => {
    const [fullName, phone] = line.split(":").map((value) => value.trim());
    return { fullName, phone };
  });
}

function defaultRestaurantForm() {
  return {
    name: "",
    city: "",
    cuisineType: "",
    amenitiesText: "",
    tables: [{ tableNumber: "", seats: "" }],
    dishes: [{ name: "", price: "" }]
  };
}

function defaultCustomerForm() {
  return {
    fullName: "",
    phone: ""
  };
}

function defaultBulkForm() {
  return {
    customersText: "Иван Петров:+79990001150\nПетр Иванов:+79990001151"
  };
}

function defaultBookingForm() {
  return {
    bookingTime: "2099-12-31T19:00",
    customerId: "",
    tableId: "",
    guestCount: "1"
  };
}

function serializeRestaurant(restaurant) {
  return {
    name: restaurant.name,
    city: restaurant.city,
    cuisineType: restaurant.cuisineType,
    amenitiesText: (restaurant.amenities || []).join(", "),
    tables: (restaurant.tables || []).map((t) => ({
      tableNumber: t.tableNumber,
      seats: t.seats
    })),
    dishes: (restaurant.dishes || []).map((d) => ({
      name: d.name,
      price: d.price
    }))
  };
}

function bookingStatusLabel(status) {
  if (status === "CONFIRMED") {
    return "Подтверждена";
  }
  if (status === "CANCELED") {
    return "Отменена";
  }
  return "Создана";
}

function importStatusLabel(status) {
  if (status === "COMPLETED") {
    return "Завершена";
  }
  if (status === "FAILED") {
    return "Ошибка";
  }
  if (status === "RUNNING") {
    return "Выполняется";
  }
  return "Ожидает запуска";
}

function formatBookingTime(value) {
  if (!value) {
    return "";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat("ru-RU", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit"
  }).format(date);
}

function humanizeErrorMessage(message) {
  if (!message) {
    return "Произошла ошибка.";
  }

  if (message.toLowerCase().includes("validation failed")) {
    return "Заполните обязательные поля формы.";
  }
  if (message.includes("must not be blank")) {
    return "Заполните обязательные поля формы.";
  }
  if (message.includes("already exists")) {
    return "Клиент с таким телефоном уже существует.";
  }
  if (message.includes("Requested seats exceed the table capacity")) {
    return "Нельзя забронировать больше мест, чем есть у выбранного стола.";
  }
  if (message.includes("Not enough free seats")) {
    return "На выбранное время свободных мест за этим столом больше нет.";
  }
  if (message.includes("Customer cannot be deleted because bookings are linked")) {
    return "Нельзя удалить клиента, пока у него есть брони.";
  }
  if (message.includes("Customer with id=")) {
    return "Клиент не найден.";
  }
  if (message.includes("Restaurant table with id=")) {
    return "Стол не найден.";
  }
  if (message.includes("Booking with id=")) {
    return "Бронь не найдена.";
  }

  return message.charAt(0).toUpperCase() + message.slice(1);
}

function validateRestaurantForm(form) {
  if (!form.name.trim() || !form.city.trim() || !form.cuisineType.trim()) {
    return "Заполните название, город и кухню.";
  }
  if (form.tables.some((table) => !String(table.tableNumber).trim() || !String(table.seats).trim())) {
    return "Заполните номер и вместимость для каждого стола.";
  }
  if (form.tables.some((table) => !Number.isInteger(Number(table.tableNumber)) || Number(table.tableNumber) <= 0)) {
    return "Номер каждого стола должен быть целым числом больше нуля.";
  }
  if (form.tables.some((table) => !Number.isInteger(Number(table.seats)) || Number(table.seats) <= 0)) {
    return "Количество мест у стола должно быть целым числом больше нуля.";
  }
  if (form.dishes.some((dish) => !dish.name.trim() || !String(dish.price).trim())) {
    return "Заполните название и цену для каждого блюда.";
  }
  if (form.dishes.some((dish) => Number.isNaN(Number(dish.price)) || Number(dish.price) <= 0)) {
    return "Цена каждого блюда должна быть больше нуля.";
  }
  return "";
}

function validateCustomerForm(form) {
  if (!form.fullName.trim() || !form.phone.trim()) {
    return "Заполните имя и телефон клиента.";
  }
  return "";
}

function validateBookingForm(form) {
  if (!form.bookingTime || !form.customerId || !form.tableId || !form.guestCount) {
    return "Выберите дату, клиента, стол и количество мест.";
  }
  if (!Number.isInteger(Number(form.guestCount)) || Number(form.guestCount) <= 0) {
    return "Количество мест должно быть целым числом больше нуля.";
  }
  return "";
}

function validateBookingCapacity(form, allTables) {
  const selectedTable = allTables.find((table) => String(table.id) === String(form.tableId));
  if (selectedTable && Number(form.guestCount) > selectedTable.seats) {
    return "Нельзя забронировать больше мест, чем есть у выбранного стола.";
  }
  return "";
}

function Modal({ open, title, onClose, children }) {
  if (!open) {
    return null;
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-card" onClick={(event) => event.stopPropagation()}>
        <div className="modal-head">
          <h3>{title}</h3>
          <button className="icon-button" type="button" onClick={onClose}>×</button>
        </div>
        {children}
      </div>
    </div>
  );
}

function App() {
  const [tab, setTab] = useState("restaurants");
  const [restaurants, setRestaurants] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [restaurantForm, setRestaurantForm] = useState(defaultRestaurantForm());
  const [customerForm, setCustomerForm] = useState(defaultCustomerForm());
  const [bulkForm, setBulkForm] = useState(defaultBulkForm());
  const [bookingForm, setBookingForm] = useState(defaultBookingForm());
  const [searchForm, setSearchForm] = useState({
    city: "",
    cuisineType: "",
    dishName: ""
  });
  const [editingRestaurantId, setEditingRestaurantId] = useState(null);
  const [taskStatus, setTaskStatus] = useState(null);
  const [restaurantModalOpen, setRestaurantModalOpen] = useState(false);
  const [customerModalOpen, setCustomerModalOpen] = useState(false);
  const [bulkModalOpen, setBulkModalOpen] = useState(false);
  const [bookingModalOpen, setBookingModalOpen] = useState(false);

  const allTables = useMemo(() => restaurants.flatMap((restaurant) =>
    (restaurant.tables || []).map((table) => ({
      ...table,
      restaurantName: restaurant.name
    }))
  ), [restaurants]);
  const activeBookings = useMemo(() =>
    bookings.filter((booking) => booking.status !== "CANCELED"),
  [bookings]);

  const visibleRestaurants = useMemo(() => {
    const city = searchForm.city.trim().toLowerCase();
    const cuisineType = searchForm.cuisineType.trim().toLowerCase();
    const dishName = searchForm.dishName.trim().toLowerCase();

    return restaurants.filter((restaurant) => {
      const cityMatches = !city || restaurant.city?.toLowerCase().includes(city);
      const cuisineMatches = !cuisineType || restaurant.cuisineType?.toLowerCase().includes(cuisineType);
      const dishMatches = !dishName || (restaurant.dishes || []).some((dish) =>
        dish.name?.toLowerCase().includes(dishName)
      );

      return cityMatches && cuisineMatches && dishMatches;
    });
  }, [restaurants, searchForm]);

  useEffect(() => {
    bootstrap(true);
  }, []);

  useEffect(() => {
    if (!message) {
      return undefined;
    }

    const timeoutId = setTimeout(() => setMessage(""), 3500);
    return () => clearTimeout(timeoutId);
  }, [message]);

  useEffect(() => {
    if (!error) {
      return undefined;
    }

    const timeoutId = setTimeout(() => setError(""), 5000);
    return () => clearTimeout(timeoutId);
  }, [error]);

  async function bootstrap(silent = false) {
    setLoading(true);
    setError("");
    try {
      const [restaurantData, customerData, bookingData] = await Promise.all([
        api.getRestaurants(),
        api.getCustomers(),
        api.getBookings()
      ]);
      setRestaurants(restaurantData);
      setCustomers(customerData);
      setBookings(bookingData);
      if (!silent) {
        setMessage("Данные обновлены.");
      }
    } catch (loadError) {
      setError(humanizeErrorMessage(loadError.message));
    } finally {
      setLoading(false);
    }
  }

  function openCreateRestaurant() {
    setEditingRestaurantId(null);
    setRestaurantForm(defaultRestaurantForm());
    setRestaurantModalOpen(true);
    setError("");
  }

  function openEditRestaurant(restaurant) {
    setEditingRestaurantId(restaurant.id);
    setRestaurantForm(serializeRestaurant(restaurant));
    setRestaurantModalOpen(true);
    setError("");
  }

  function closeRestaurantModal() {
    setRestaurantModalOpen(false);
    setEditingRestaurantId(null);
    setRestaurantForm(defaultRestaurantForm());
  }

  async function submitRestaurant(event) {
    event.preventDefault();
    setError("");
    setMessage("");

    try {
      const validationError = validateRestaurantForm(restaurantForm);
      if (validationError) {
        throw new Error(validationError);
      }
      const payload = parseRestaurantPayload(restaurantForm);
      const savedRestaurant = editingRestaurantId
        ? await api.updateRestaurant(editingRestaurantId, payload)
        : await api.createRestaurant(payload);

      setMessage(editingRestaurantId
        ? `Информация о ресторане "${savedRestaurant.name}" обновлена.`
        : `Ресторан "${savedRestaurant.name}" добавлен.`);
      closeRestaurantModal();
      await bootstrap(true);
    } catch (submitError) {
      setError(humanizeErrorMessage(submitError.message));
    }
  }

  async function removeRestaurant(id) {
    setError("");
    setMessage("");

    try {
      await api.deleteRestaurant(id);
      setMessage("Ресторан удален.");
      await bootstrap(true);
    } catch (deleteError) {
      setError(humanizeErrorMessage(deleteError.message));
    }
  }

  function resetSearch() {
    setSearchForm({
      city: "",
      cuisineType: "",
      dishName: ""
    });
    setMessage("Показан полный список ресторанов.");
  }

  async function submitCustomer(event) {
    event.preventDefault();
    setError("");
    setMessage("");

    try {
      const validationError = validateCustomerForm(customerForm);
      if (validationError) {
        throw new Error(validationError);
      }
      const customer = await api.createCustomer(customerForm);
      setMessage(`Клиент "${customer.fullName}" добавлен.`);
      setCustomerModalOpen(false);
      setCustomerForm(defaultCustomerForm());
      await bootstrap(true);
    } catch (createError) {
      setError(humanizeErrorMessage(createError.message));
    }
  }

  async function submitBulkImport(event) {
    event.preventDefault();
    setError("");
    setMessage("");

    try {
      const task = await api.startCustomerImport({
        customers: parseBulkCustomers(bulkForm.customersText)
      });
      setTaskStatus(task);
      setBulkModalOpen(false);
      setMessage("Список гостей отправлен в обработку.");
    } catch (bulkError) {
      setError(humanizeErrorMessage(bulkError.message));
    }
  }

  async function refreshTaskStatus() {
    if (!taskStatus?.taskId) {
      return;
    }

    setError("");
    try {
      const freshStatus = await api.getImportStatus(taskStatus.taskId);
      setTaskStatus(freshStatus);
      setMessage(`Статус загрузки: ${importStatusLabel(freshStatus.status)}.`);
      await bootstrap(true);
    } catch (taskError) {
      setError(humanizeErrorMessage(taskError.message));
    }
  }

  async function submitBooking(event) {
    event.preventDefault();
    setError("");
    setMessage("");

    try {
      const validationError = validateBookingForm(bookingForm);
      if (validationError) {
        throw new Error(validationError);
      }
      const capacityError = validateBookingCapacity(bookingForm, allTables);
      if (capacityError) {
        throw new Error(capacityError);
      }
      await api.createBooking({
        bookingTime: `${bookingForm.bookingTime}:00`,
        status: "CREATED",
        customerId: Number(bookingForm.customerId),
        tableId: Number(bookingForm.tableId),
        guestCount: Number(bookingForm.guestCount)
      });
      setMessage("Бронь создана.");
      setBookingModalOpen(false);
      setBookingForm(defaultBookingForm());
      await bootstrap(true);
    } catch (bookingError) {
      setError(humanizeErrorMessage(bookingError.message));
    }
  }

  async function updateBookingStatus(id, status) {
    setError("");
    setMessage("");

    try {
      await api.updateBookingStatus(id, status);
      setMessage("Бронь отменена.");
      await bootstrap(true);
    } catch (statusError) {
      setError(humanizeErrorMessage(statusError.message));
    }
  }

  async function removeCustomer(id, fullName) {
    setError("");
    setMessage("");

    try {
      await api.deleteCustomer(id);
      setMessage(`Клиент "${fullName}" удален.`);
      await bootstrap(true);
    } catch (deleteError) {
      setError(humanizeErrorMessage(deleteError.message));
    }
  }

  return (
    <div className="app-shell">
      <section className="hero hero-simple">
        <div className="hero-card hero-card-wide">
          <div className="eyebrow">Ресторанный гид</div>
          <h1>Выберите место для отдыха</h1>
          <p>
            Смотрите заведения, находите подходящую кухню, добавляйте гостей
            и управляйте бронированиями в одном окне.
          </p>
        </div>
      </section>

      <div className="nav-tabs">
        {[
          ["restaurants", "Рестораны"],
          ["customers", "Клиенты"],
          ["bookings", "Бронирования"]
        ].map(([value, label]) => (
          <button
            key={value}
            className={`tab ${tab === value ? "active" : ""}`}
            onClick={() => setTab(value)}
            type="button"
          >
            {label}
          </button>
        ))}
      </div>

      {(message || error) && (
        <div className="notice-stack">
          {message && (
            <div className="notice notice-floating">
              <div className="notice-title">ГОТОВО</div>
              <div className="notice-body">{message}</div>
            </div>
          )}
          {error && (
            <div className="notice error notice-floating">
              <div className="notice-title">ОШИБКА</div>
              <div className="notice-body">{error}</div>
            </div>
          )}
        </div>
      )}

      {tab === "restaurants" && (
        <div className="layout">
          <aside className="panel stack">
            <div className="section-head section-head-column">
              <div>
                <h2>Фильтры</h2>
                <p className="meta">Уточните поиск по городу, кухне и блюду.</p>
              </div>
            </div>

            <div className="stack">
              <div className="field">
                <label>Город</label>
                <input
                  value={searchForm.city}
                  placeholder="Например, Moscow"
                  onChange={(event) => setSearchForm({ ...searchForm, city: event.target.value })}
                />
              </div>
              <div className="field">
                <label>Кухня</label>
                <input
                  value={searchForm.cuisineType}
                  placeholder="Например, Italian"
                  onChange={(event) => setSearchForm({ ...searchForm, cuisineType: event.target.value })}
                />
              </div>
              <div className="field">
                <label>Блюдо</label>
                <input
                  value={searchForm.dishName}
                  placeholder="Например, pasta"
                  onChange={(event) => setSearchForm({ ...searchForm, dishName: event.target.value })}
                />
              </div>
              <div className="actions">
                <button className="button ghost" type="button" onClick={resetSearch}>Сбросить</button>
              </div>
              {(searchForm.city || searchForm.cuisineType || searchForm.dishName) && (
                <div className="subcard">
                  Найдено: {visibleRestaurants.length}.
                </div>
              )}
            </div>
          </aside>

          <main className="results">
            <div className="section-head">
              <div>
                <h2>Список ресторанов</h2>
              </div>
              <button className="button" type="button" onClick={openCreateRestaurant}>Добавить ресторан</button>
            </div>

            {visibleRestaurants.length === 0 && <div className="empty">Ничего не найдено. Попробуйте изменить фильтры.</div>}
            {visibleRestaurants.map((restaurant) => (
              <article className="card" key={restaurant.id}>
                <div className="card-head">
                  <div>
                    <h3>{restaurant.name}</h3>
                    <div className="meta">{restaurant.city} · {restaurant.cuisineType}</div>
                    <div className="chips">
                      {(restaurant.amenities || []).map((amenity) => (
                        <span className="chip" key={`${restaurant.id}-${amenity}`}>{amenity}</span>
                      ))}
                    </div>
                  </div>
                  {"tables" in restaurant && (
                    <div className="actions">
                      <button className="button ghost" type="button" onClick={() => openEditRestaurant(restaurant)}>
                        Изменить
                      </button>
                      <button className="button danger" type="button" onClick={() => removeRestaurant(restaurant.id)}>
                        Удалить
                      </button>
                    </div>
                  )}
                </div>

                {"tables" in restaurant && (
                  <div className="lists">
                    <div className="subcard">
                      <strong>Столы</strong>
                      <ul>
                        {(restaurant.tables || []).map((table) => (
                          <li key={table.id}>Стол №{table.tableNumber}, мест: {table.seats}</li>
                        ))}
                      </ul>
                    </div>
                    <div className="subcard">
                      <strong>Меню</strong>
                      <ul>
                        {(restaurant.dishes || []).map((dish) => (
                          <li key={dish.id}>{dish.name} · {dish.price}</li>
                        ))}
                      </ul>
                    </div>
                    <div className="subcard">
                      <strong>Удобства</strong>
                      <ul>
                        {(restaurant.amenities || []).map((amenity) => (
                          <li key={amenity}>{amenity}</li>
                        ))}
                      </ul>
                    </div>
                  </div>
                )}
              </article>
            ))}
          </main>
        </div>
      )}

      {tab === "customers" && (
        <div className="layout">
          <aside className="panel stack">
            <div className="section-head section-head-column">
              <div>
                <h2>Клиенты</h2>
                <p className="meta">Храните контакты и при необходимости загружайте список сразу.</p>
              </div>
              <div className="actions">
                <button className="button" type="button" onClick={() => setCustomerModalOpen(true)}>Добавить клиента</button>
                <button className="button secondary" type="button" onClick={() => setBulkModalOpen(true)}>Загрузить список</button>
              </div>
            </div>

            {taskStatus && (
              <div className="subcard">
                <strong>Загрузка списка</strong>
                <p className="meta">Статус: {importStatusLabel(taskStatus.status)}</p>
                <p className="meta">Обработано: {taskStatus.savedCount ?? 0} из {taskStatus.requestedCount ?? 0}</p>
                <p>{taskStatus.note || "Операция выполняется."}</p>
                <button className="button ghost" type="button" onClick={refreshTaskStatus}>Обновить статус</button>
              </div>
            )}
          </aside>

          <main className="results">
          {customers.length === 0 && <div className="empty">Список клиентов пока пуст.</div>}
          {customers.map((customer) => (
            <article className="card" key={customer.id}>
              <div className="card-head">
                <div>
                  <h3>{customer.fullName}</h3>
                  <div className="meta">{customer.phone}</div>
                </div>
                <div className="actions">
                  <button className="button danger" type="button" onClick={() => removeCustomer(customer.id, customer.fullName)}>
                    Удалить
                  </button>
                </div>
              </div>
            </article>
          ))}
          </main>
        </div>
      )}

      {tab === "bookings" && (
        <div className="layout">
          <aside className="panel stack">
            <div className="section-head section-head-column">
              <div>
                <h2>Бронирования</h2>
                <p className="meta">Просматривайте текущие брони и создавайте новые заявки.</p>
              </div>
              <button className="button" type="button" onClick={() => setBookingModalOpen(true)}>Новая бронь</button>
            </div>
          </aside>

          <main className="results">
            {activeBookings.length === 0 && <div className="empty">Бронирований пока нет.</div>}
            {activeBookings.map((booking) => (
              <article className="card" key={booking.id}>
                <div className="card-head">
                  <div>
                    <h3>{booking.restaurantName}</h3>
                    <div className="meta">Стол {booking.tableNumber} · {booking.customerName}</div>
                    <div className="meta">{formatBookingTime(booking.bookingTime)}</div>
                    <div className="meta">Мест: {booking.guestCount}</div>
                  </div>
                  <div className="actions">
                    <button className="button danger" type="button" onClick={() => updateBookingStatus(booking.id, "CANCELED")}>
                      Отменить
                    </button>
                  </div>
                </div>
              </article>
            ))}
          </main>
        </div>
      )}

      <Modal
        open={restaurantModalOpen}
        title={editingRestaurantId ? "Изменить ресторан" : "Добавить ресторан"}
        onClose={closeRestaurantModal}
      >
        <form className="stack" onSubmit={submitRestaurant}>
          <div className="field">
            <label>Название</label>
            <input
              value={restaurantForm.name}
              onChange={(event) => setRestaurantForm({ ...restaurantForm, name: event.target.value })}
              required
            />
          </div>
          <div className="grid-two">
            <div className="field">
              <label>Город</label>
              <input
                value={restaurantForm.city}
                onChange={(event) => setRestaurantForm({ ...restaurantForm, city: event.target.value })}
                required
              />
            </div>
            <div className="field">
              <label>Кухня</label>
              <input
                value={restaurantForm.cuisineType}
                onChange={(event) => setRestaurantForm({ ...restaurantForm, cuisineType: event.target.value })}
                required
              />
            </div>
          </div>
          <div className="field">
            <label>Удобства</label>
            <input
              value={restaurantForm.amenitiesText}
              placeholder="Например: WiFi, Терраса"
              onChange={(event) => setRestaurantForm({ ...restaurantForm, amenitiesText: event.target.value })}
            />
          </div>
          <div className="field">
            <label>Столы</label>
              {restaurantForm.tables.map((table, index) => (
              <div className="row-with-delete" key={index}>
                <input
                  placeholder="Номер стола"
                  value={table.tableNumber}
                  onChange={(e) => {
                    const updated = [...restaurantForm.tables];
                    updated[index].tableNumber = e.target.value;
                    setRestaurantForm({ ...restaurantForm, tables: updated });
                  }}
                />
                <input
                  placeholder="Вместимость"
                  value={table.seats}
                  onChange={(e) => {
                    const updated = [...restaurantForm.tables];
                    updated[index].seats = e.target.value;
                    setRestaurantForm({ ...restaurantForm, tables: updated });
                  }}
                />

                  <button
                    type="button"
                    className="icon-button danger"
                    onClick={() => {
                      const updated = restaurantForm.tables.filter((_, i) => i !== index);
                      setRestaurantForm({ ...restaurantForm, tables: updated });
                    }}
                  >
                    ×
                  </button>
              </div>
            ))}

            <button
              type="button"
              className="button ghost"
              onClick={() =>
                setRestaurantForm({
                  ...restaurantForm,
                  tables: [...restaurantForm.tables, { tableNumber: "", seats: "" }]
                })
              }
            >
              + Добавить стол
            </button>
          </div>
          <div className="field">
            <label>Меню</label>
              {restaurantForm.dishes.map((dish, index) => (
                <div className="row-with-delete" key={index}>
                  <input
                    placeholder="Название блюда"
                    value={dish.name}
                    onChange={(e) => {
                      const updated = [...restaurantForm.dishes];
                      updated[index].name = e.target.value;
                      setRestaurantForm({ ...restaurantForm, dishes: updated });
                    }}
                  />
                  <input
                    placeholder="Цена"
                    value={dish.price}
                    onChange={(e) => {
                      const updated = [...restaurantForm.dishes];
                      updated[index].price = e.target.value;
                      setRestaurantForm({ ...restaurantForm, dishes: updated });
                    }}
                  />

                    <button
                      type="button"
                      className="icon-button danger"
                      onClick={() => {
                        const updated = restaurantForm.dishes.filter((_, i) => i !== index);
                        setRestaurantForm({ ...restaurantForm, dishes: updated });
                      }}
                    >
                      ×
                    </button>
                </div>
              ))}

              <button
                type="button"
                className="button ghost"
                onClick={() =>
                  setRestaurantForm({
                    ...restaurantForm,
                    dishes: [...restaurantForm.dishes, { name: "", price: "" }]
                  })
                }
              >
                + Добавить блюдо
              </button>
          </div>
          <div className="actions">
            <button className="button" type="submit">{editingRestaurantId ? "Сохранить" : "Добавить"}</button>
            <button className="button ghost" type="button" onClick={closeRestaurantModal}>Отмена</button>
          </div>
        </form>
      </Modal>

      <Modal open={customerModalOpen} title="Добавить клиента" onClose={() => setCustomerModalOpen(false)}>
        <form className="stack" onSubmit={submitCustomer}>
          <div className="field">
            <label>Имя</label>
            <input
              value={customerForm.fullName}
              onChange={(event) => setCustomerForm({ ...customerForm, fullName: event.target.value })}
              required
            />
          </div>
          <div className="field">
            <label>Телефон</label>
            <input
              value={customerForm.phone}
              onChange={(event) => setCustomerForm({ ...customerForm, phone: event.target.value })}
              required
            />
          </div>
          <div className="actions">
            <button className="button" type="submit">Сохранить</button>
            <button className="button ghost" type="button" onClick={() => setCustomerModalOpen(false)}>Отмена</button>
          </div>
        </form>
      </Modal>

      <Modal open={bulkModalOpen} title="Загрузить список клиентов" onClose={() => setBulkModalOpen(false)}>
        <form className="stack" onSubmit={submitBulkImport}>
          <div className="field">
            <label>Список клиентов</label>
            <textarea
              value={bulkForm.customersText}
              placeholder={"Каждая строка: имя:телефон\nНапример: Иван Петров:+79990001150"}
              onChange={(event) => setBulkForm({ customersText: event.target.value })}
            />
          </div>
          <div className="actions">
            <button className="button secondary" type="submit">Запустить</button>
            <button className="button ghost" type="button" onClick={() => setBulkModalOpen(false)}>Отмена</button>
          </div>
        </form>
      </Modal>

      <Modal open={bookingModalOpen} title="Новая бронь" onClose={() => setBookingModalOpen(false)}>
        <form className="stack" onSubmit={submitBooking}>
          <div className="field">
            <label>Дата и время</label>
            <input
              type="datetime-local"
              value={bookingForm.bookingTime}
              onChange={(event) => setBookingForm({ ...bookingForm, bookingTime: event.target.value })}
              required
            />
          </div>
          <div className="field">
            <label>Сколько мест</label>
            <input
              type="number"
              min="1"
              value={bookingForm.guestCount}
              onChange={(event) => setBookingForm({ ...bookingForm, guestCount: event.target.value })}
              required
            />
          </div>
          <div className="field">
            <label>Клиент</label>
            <select
              value={bookingForm.customerId}
              onChange={(event) => setBookingForm({ ...bookingForm, customerId: event.target.value })}
              required
            >
              <option value="">Выберите клиента</option>
              {customers.map((customer) => (
                <option key={customer.id} value={customer.id}>
                  {customer.fullName}
                </option>
              ))}
            </select>
          </div>
          <div className="field">
            <label>Стол</label>
            <select
              value={bookingForm.tableId}
              onChange={(event) => setBookingForm({ ...bookingForm, tableId: event.target.value })}
              required
            >
              <option value="">Выберите стол</option>
              {allTables.map((table) => (
                <option key={table.id} value={table.id}>
                  {table.restaurantName}, стол {table.tableNumber}
                </option>
              ))}
            </select>
          </div>
          <div className="actions">
            <button className="button" type="submit">Забронировать</button>
            <button className="button ghost" type="button" onClick={() => setBookingModalOpen(false)}>Отмена</button>
          </div>
        </form>
      </Modal>
    </div>
  );
}

ReactDOM.createRoot(document.getElementById("root")).render(<App />);
