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
      const message = errorPayload?.message || `${response.status} ${response.statusText}`;
      throw new Error(message);
    }

    if (response.status === 204) {
      return null;
    }

    return response.json();
  },
  getRestaurants() {
    return this.request("/api/v1/restaurants/all");
  },
  searchRestaurants(query, mode) {
    return this.request(`/api/v1/restaurants/search/${mode}?${query}`);
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
  startAsyncBulk(payload) {
    return this.request("/api/v1/customers/bulk/async", {
      method: "POST",
      body: JSON.stringify(payload)
    });
  },
  getTaskStatus(taskId) {
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
  const tables = parseLines(form.tablesText, (line) => {
    const [tableNumber, seats] = line.split(":").map((value) => value.trim());
    return { tableNumber: Number(tableNumber), seats: Number(seats) };
  });

  const dishes = parseLines(form.dishesText, (line) => {
    const [name, price] = line.split(":").map((value) => value.trim());
    return { name, price: Number(price) };
  });

  const amenities = form.amenitiesText
    .split(",")
    .map((value) => value.trim())
    .filter(Boolean);

  return {
    name: form.name.trim(),
    city: form.city.trim(),
    cuisineType: form.cuisineType.trim(),
    tables,
    dishes,
    amenities
  };
}

function serializeRestaurant(restaurant) {
  return {
    name: restaurant.name,
    city: restaurant.city,
    cuisineType: restaurant.cuisineType,
    amenitiesText: (restaurant.amenities || []).join(", "),
    tablesText: (restaurant.tables || [])
      .map((table) => `${table.tableNumber}:${table.seats}`)
      .join("\n"),
    dishesText: (restaurant.dishes || [])
      .map((dish) => `${dish.name}:${dish.price}`)
      .join("\n")
  };
}

function defaultRestaurantForm() {
  return {
    name: "",
    city: "Moscow",
    cuisineType: "",
    amenitiesText: "",
    tablesText: "1:2",
    dishesText: "Pasta:14"
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
    customersText: "Ivan Petrov:+79990001150\nPetr Ivanov:+79990001151"
  };
}

function parseBulkCustomers(text) {
  return parseLines(text, (line) => {
    const [fullName, phone] = line.split(":").map((value) => value.trim());
    return { fullName, phone };
  });
}

function App() {
  const [tab, setTab] = useState("restaurants");
  const [restaurants, setRestaurants] = useState([]);
  const [searchResults, setSearchResults] = useState([]);
  const [searchMeta, setSearchMeta] = useState(null);
  const [customers, setCustomers] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [taskStatus, setTaskStatus] = useState(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [searchMode, setSearchMode] = useState("jpql");
  const [searchForm, setSearchForm] = useState({
    city: "",
    cuisineType: "",
    dishName: "",
    minDishPrice: "",
    maxDishPrice: ""
  });
  const [restaurantForm, setRestaurantForm] = useState(defaultRestaurantForm());
  const [editingRestaurantId, setEditingRestaurantId] = useState(null);
  const [customerForm, setCustomerForm] = useState(defaultCustomerForm());
  const [bulkForm, setBulkForm] = useState(defaultBulkForm());
  const [bookingForm, setBookingForm] = useState({
    bookingTime: "2099-12-31T19:00",
    customerId: "",
    tableId: ""
  });

  const restaurantStats = useMemo(() => ({
    restaurants: restaurants.length,
    tables: restaurants.flatMap((restaurant) => restaurant.tables || []).length,
    dishes: restaurants.flatMap((restaurant) => restaurant.dishes || []).length,
    amenities: new Set(restaurants.flatMap((restaurant) => restaurant.amenities || [])).size
  }), [restaurants]);

  useEffect(() => {
    bootstrap();
  }, []);

  async function bootstrap() {
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
      setMessage("SPA подключена к Spring Boot API и готова к демонстрации.");
    } catch (loadError) {
      setError(loadError.message);
    } finally {
      setLoading(false);
    }
  }

  function handleRestaurantFormChange(event) {
    const { name, value } = event.target;
    setRestaurantForm((current) => ({ ...current, [name]: value }));
  }

  function handleCustomerFormChange(event) {
    const { name, value } = event.target;
    setCustomerForm((current) => ({ ...current, [name]: value }));
  }

  function handleSearchChange(event) {
    const { name, value } = event.target;
    setSearchForm((current) => ({ ...current, [name]: value }));
  }

  function handleBookingFormChange(event) {
    const { name, value } = event.target;
    setBookingForm((current) => ({ ...current, [name]: value }));
  }

  async function submitRestaurant(event) {
    event.preventDefault();
    setError("");
    setMessage("");

    try {
      const payload = parseRestaurantPayload(restaurantForm);
      const savedRestaurant = editingRestaurantId
        ? await api.updateRestaurant(editingRestaurantId, payload)
        : await api.createRestaurant(payload);

      setMessage(editingRestaurantId
        ? `Ресторан ${savedRestaurant.name} обновлён`
        : `Ресторан ${savedRestaurant.name} создан`);
      setRestaurantForm(defaultRestaurantForm());
      setEditingRestaurantId(null);
      await bootstrap();
    } catch (submitError) {
      setError(submitError.message);
    }
  }

  function startEditRestaurant(restaurant) {
    setTab("restaurants");
    setEditingRestaurantId(restaurant.id);
    setRestaurantForm(serializeRestaurant(restaurant));
    setMessage(`Редактируется ресторан #${restaurant.id}`);
    setError("");
  }

  async function removeRestaurant(id) {
    setError("");
    setMessage("");
    try {
      await api.deleteRestaurant(id);
      setMessage(`Ресторан #${id} удалён`);
      if (editingRestaurantId === id) {
        setEditingRestaurantId(null);
        setRestaurantForm(defaultRestaurantForm());
      }
      await bootstrap();
    } catch (deleteError) {
      setError(deleteError.message);
    }
  }

  async function runSearch(event) {
    event.preventDefault();
    setError("");
    setMessage("");

    const params = new URLSearchParams();
    Object.entries(searchForm).forEach(([key, value]) => {
      if (value.trim()) {
        params.set(key, value.trim());
      }
    });
    params.set("page", "0");
    params.set("size", "10");
    params.set("sort", "name,asc");

    try {
      const page = await api.searchRestaurants(params.toString(), searchMode);
      setSearchResults(page.content || []);
      setSearchMeta(page);
      setMessage(`Фильтрация через ${searchMode.toUpperCase()} вернула ${page.totalElements ?? 0} записей`);
    } catch (searchError) {
      setError(searchError.message);
    }
  }

  async function createCustomer(event) {
    event.preventDefault();
    setError("");
    setMessage("");
    try {
      const customer = await api.createCustomer(customerForm);
      setMessage(`Клиент ${customer.fullName} создан`);
      setCustomerForm(defaultCustomerForm());
      await bootstrap();
    } catch (createError) {
      setError(createError.message);
    }
  }

  async function startAsyncBulk(event) {
    event.preventDefault();
    setError("");
    setMessage("");
    try {
      const task = await api.startAsyncBulk({
        customers: parseBulkCustomers(bulkForm.customersText)
      });
      setTaskStatus(task);
      setMessage(`Async bulk запущен. taskId=${task.taskId}`);
    } catch (bulkError) {
      setError(bulkError.message);
    }
  }

  async function refreshTaskStatus() {
    if (!taskStatus?.taskId) {
      return;
    }
    setError("");
    try {
      const freshStatus = await api.getTaskStatus(taskStatus.taskId);
      setTaskStatus(freshStatus);
      setMessage(`Статус задачи ${freshStatus.taskId}: ${freshStatus.status}`);
      await bootstrap();
    } catch (taskError) {
      setError(taskError.message);
    }
  }

  async function createBooking(event) {
    event.preventDefault();
    setError("");
    setMessage("");
    try {
      const booking = await api.createBooking({
        bookingTime: `${bookingForm.bookingTime}:00`,
        status: "CREATED",
        customerId: Number(bookingForm.customerId),
        tableId: Number(bookingForm.tableId)
      });
      setMessage(`Бронь #${booking.id} создана`);
      await bootstrap();
    } catch (bookingError) {
      setError(bookingError.message);
    }
  }

  async function updateBookingStatus(id, status) {
    setError("");
    setMessage("");
    try {
      const updatedBooking = await api.updateBookingStatus(id, status);
      setMessage(`Статус брони #${updatedBooking.id} изменён на ${updatedBooking.status}`);
      await bootstrap();
    } catch (statusError) {
      setError(statusError.message);
    }
  }

  const allTables = restaurants.flatMap((restaurant) =>
    (restaurant.tables || []).map((table) => ({
      ...table,
      restaurantName: restaurant.name
    }))
  );

  return (
    <div className="app-shell">
      <section className="hero">
        <div className="hero-card">
          <div className="eyebrow">SPA Client / React</div>
          <h1>Restaurant Control Room</h1>
          <p>
            Один клиент поверх API из лабораторных: CRUD по ресторанам, фильтрация поиска,
            связи OneToMany через столы и блюда, ManyToMany через amenities,
            плюс клиенты, бронирования и async bulk-задачи.
          </p>
        </div>
        <div className="hero-stats">
          <div className="stat"><strong>{restaurantStats.restaurants}</strong>ресторанов</div>
          <div className="stat"><strong>{restaurantStats.tables}</strong>столов</div>
          <div className="stat"><strong>{restaurantStats.dishes}</strong>блюд</div>
          <div className="stat"><strong>{restaurantStats.amenities}</strong>amenities</div>
        </div>
      </section>

      <div className="nav-tabs">
        {[
          ["restaurants", "Restaurants"],
          ["customers", "Customers"],
          ["bookings", "Bookings"]
        ].map(([value, label]) => (
          <button
            key={value}
            className={`tab ${tab === value ? "active" : ""}`}
            onClick={() => setTab(value)}
          >
            {label}
          </button>
        ))}
      </div>

      {message && <div className="notice">{message}</div>}
      {error && <div className="notice error">{error}</div>}
      {loading && <div className="notice">Загрузка данных из backend...</div>}

      {tab === "restaurants" && (
        <div className="layout">
          <aside className="panel stack">
            <div>
              <h2>{editingRestaurantId ? "Редактирование ресторана" : "Создать ресторан"}</h2>
              <p className="meta">CRUD для основной сущности проекта.</p>
            </div>
            <form className="stack" onSubmit={submitRestaurant}>
              <div className="field">
                <label>Название</label>
                <input name="name" value={restaurantForm.name} onChange={handleRestaurantFormChange} required />
              </div>
              <div className="grid-two">
                <div className="field">
                  <label>Город</label>
                  <input name="city" value={restaurantForm.city} onChange={handleRestaurantFormChange} required />
                </div>
                <div className="field">
                  <label>Кухня</label>
                  <input name="cuisineType" value={restaurantForm.cuisineType} onChange={handleRestaurantFormChange} required />
                </div>
              </div>
              <div className="field">
                <label>Amenities, через запятую (ManyToMany)</label>
                <input
                  name="amenitiesText"
                  value={restaurantForm.amenitiesText}
                  onChange={handleRestaurantFormChange}
                  placeholder="WiFi, Terrace, Parking"
                />
              </div>
              <div className="field">
                <label>Tables, формат `tableNumber:seats` по строкам (OneToMany)</label>
                <textarea name="tablesText" value={restaurantForm.tablesText} onChange={handleRestaurantFormChange} />
              </div>
              <div className="field">
                <label>Dishes, формат `name:price` по строкам (OneToMany)</label>
                <textarea name="dishesText" value={restaurantForm.dishesText} onChange={handleRestaurantFormChange} />
              </div>
              <div className="actions">
                <button className="button" type="submit">
                  {editingRestaurantId ? "Обновить" : "Создать"}
                </button>
                {editingRestaurantId && (
                  <button
                    className="button ghost"
                    type="button"
                    onClick={() => {
                      setEditingRestaurantId(null);
                      setRestaurantForm(defaultRestaurantForm());
                    }}
                  >
                    Сбросить
                  </button>
                )}
              </div>
            </form>

            <div>
              <h3>Фильтрация</h3>
              <p className="meta">Работает с `/search/jpql` и `/search/native`.</p>
            </div>
            <form className="stack" onSubmit={runSearch}>
              <div className="grid-two">
                <div className="field">
                  <label>Mode</label>
                  <select value={searchMode} onChange={(event) => setSearchMode(event.target.value)}>
                    <option value="jpql">JPQL</option>
                    <option value="native">Native</option>
                  </select>
                </div>
                <div className="field">
                  <label>City</label>
                  <input name="city" value={searchForm.city} onChange={handleSearchChange} />
                </div>
              </div>
              <div className="grid-two">
                <div className="field">
                  <label>Cuisine</label>
                  <input name="cuisineType" value={searchForm.cuisineType} onChange={handleSearchChange} />
                </div>
                <div className="field">
                  <label>Dish Name</label>
                  <input name="dishName" value={searchForm.dishName} onChange={handleSearchChange} />
                </div>
              </div>
              <div className="grid-two">
                <div className="field">
                  <label>Min Price</label>
                  <input name="minDishPrice" value={searchForm.minDishPrice} onChange={handleSearchChange} />
                </div>
                <div className="field">
                  <label>Max Price</label>
                  <input name="maxDishPrice" value={searchForm.maxDishPrice} onChange={handleSearchChange} />
                </div>
              </div>
              <div className="actions">
                <button className="button secondary" type="submit">Запустить поиск</button>
              </div>
              {searchMeta && (
                <div className="notice">
                  Найдено: {searchMeta.totalElements}, страниц: {searchMeta.totalPages}
                </div>
              )}
              {searchResults.length > 0 && (
                <div className="subcard">
                  <strong>Результаты фильтрации</strong>
                  <ul>
                    {searchResults.map((restaurant) => (
                      <li key={restaurant.id}>
                        #{restaurant.id} {restaurant.name} / {restaurant.city} / {restaurant.cuisineType}
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </form>
          </aside>

          <main className="results">
            {restaurants.length === 0 && <div className="empty">Ресторанов пока нет.</div>}
            {restaurants.map((restaurant) => (
              <article className="card" key={restaurant.id}>
                <div className="card-head">
                  <div>
                    <h3>{restaurant.name}</h3>
                    <div className="meta">#{restaurant.id} · {restaurant.city} · {restaurant.cuisineType}</div>
                    <div className="chips">
                      {(restaurant.amenities || []).map((amenity) => (
                        <span className="chip" key={`${restaurant.id}-${amenity}`}>{amenity}</span>
                      ))}
                    </div>
                  </div>
                  <div className="actions">
                    <button className="button ghost" onClick={() => startEditRestaurant(restaurant)}>Редактировать</button>
                    <button className="button danger" onClick={() => removeRestaurant(restaurant.id)}>Удалить</button>
                  </div>
                </div>
                <div className="lists">
                  <div className="subcard">
                    <strong>Tables (OneToMany)</strong>
                    <ul>
                      {(restaurant.tables || []).map((table) => (
                        <li key={table.id}>Стол #{table.tableNumber}, мест: {table.seats}, id={table.id}</li>
                      ))}
                    </ul>
                  </div>
                  <div className="subcard">
                    <strong>Dishes (OneToMany)</strong>
                    <ul>
                      {(restaurant.dishes || []).map((dish) => (
                        <li key={dish.id}>{dish.name} · {dish.price}</li>
                      ))}
                    </ul>
                  </div>
                  <div className="subcard">
                    <strong>Amenities (ManyToMany)</strong>
                    <ul>
                      {(restaurant.amenities || []).map((amenity) => (
                        <li key={amenity}>{amenity}</li>
                      ))}
                    </ul>
                  </div>
                </div>
              </article>
            ))}
          </main>
        </div>
      )}

      {tab === "customers" && (
        <div className="layout">
          <aside className="panel stack">
            <div>
              <h2>Клиенты и async bulk</h2>
              <p className="meta">Работа с `customers` API и async-операцией.</p>
            </div>
            <form className="stack" onSubmit={createCustomer}>
              <div className="field">
                <label>Full Name</label>
                <input name="fullName" value={customerForm.fullName} onChange={handleCustomerFormChange} required />
              </div>
              <div className="field">
                <label>Phone</label>
                <input name="phone" value={customerForm.phone} onChange={handleCustomerFormChange} required />
              </div>
              <button className="button" type="submit">Создать клиента</button>
            </form>

            <form className="stack" onSubmit={startAsyncBulk}>
              <div>
                <h3>Async bulk</h3>
                <p className="meta">По строке: `Full Name:+7999...`</p>
              </div>
              <div className="field">
                <label>Customers</label>
                <textarea
                  value={bulkForm.customersText}
                  onChange={(event) => setBulkForm({ customersText: event.target.value })}
                />
              </div>
              <div className="actions">
                <button className="button secondary" type="submit">Стартовать async bulk</button>
                <button className="button ghost" type="button" onClick={refreshTaskStatus}>Обновить task status</button>
              </div>
              {taskStatus && (
                <div className="subcard">
                  <strong>Task #{taskStatus.taskId}</strong>
                  <p className="meta">Status: {taskStatus.status}</p>
                  <p className="meta">Saved: {taskStatus.savedCount ?? 0} / {taskStatus.requestedCount ?? 0}</p>
                  <p>{taskStatus.note || "Без комментария"}</p>
                </div>
              )}
            </form>
          </aside>

          <main className="results">
            {customers.length === 0 && <div className="empty">Клиентов пока нет.</div>}
            {customers.map((customer) => (
              <article className="card" key={customer.id}>
                <h3>{customer.fullName}</h3>
                <div className="meta">#{customer.id} · {customer.phone}</div>
              </article>
            ))}
          </main>
        </div>
      )}

      {tab === "bookings" && (
        <div className="layout">
          <aside className="panel stack">
            <div>
              <h2>Бронирования</h2>
              <p className="meta">Create + list + update status поверх booking API.</p>
            </div>
            <form className="stack" onSubmit={createBooking}>
              <div className="field">
                <label>Booking Time</label>
                <input
                  type="datetime-local"
                  name="bookingTime"
                  value={bookingForm.bookingTime}
                  onChange={handleBookingFormChange}
                  required
                />
              </div>
              <div className="field">
                <label>Customer</label>
                <select name="customerId" value={bookingForm.customerId} onChange={handleBookingFormChange} required>
                  <option value="">Выбери клиента</option>
                  {customers.map((customer) => (
                    <option key={customer.id} value={customer.id}>
                      #{customer.id} {customer.fullName}
                    </option>
                  ))}
                </select>
              </div>
              <div className="field">
                <label>Table</label>
                <select name="tableId" value={bookingForm.tableId} onChange={handleBookingFormChange} required>
                  <option value="">Выбери стол</option>
                  {allTables.map((table) => (
                    <option key={table.id} value={table.id}>
                      #{table.id} · {table.restaurantName} · стол {table.tableNumber}
                    </option>
                  ))}
                </select>
              </div>
              <button className="button" type="submit">Создать бронь</button>
            </form>
          </aside>

          <main className="results">
            {bookings.length === 0 && <div className="empty">Бронирований пока нет.</div>}
            {bookings.map((booking) => (
              <article className="card" key={booking.id}>
                <div className="card-head">
                  <div>
                    <h3>Бронь #{booking.id}</h3>
                    <div className="meta">
                      {booking.restaurantName} · стол {booking.tableNumber} · клиент {booking.customerName}
                    </div>
                    <div className="meta">{booking.bookingTime} · статус {booking.status}</div>
                  </div>
                  <div className="actions">
                    <button className="button ghost" onClick={() => updateBookingStatus(booking.id, "CONFIRMED")}>
                      Confirm
                    </button>
                    <button className="button danger" onClick={() => updateBookingStatus(booking.id, "CANCELED")}>
                      Cancel
                    </button>
                  </div>
                </div>
              </article>
            ))}
          </main>
        </div>
      )}
    </div>
  );
}

ReactDOM.createRoot(document.getElementById("root")).render(<App />);
