# Карта Фронтенда

Этот файл нужен как быстрый справочник по тому, что видно пользователю на странице, где это реализовано и как оно работает.

## Общая структура

Точка входа фронтенда:
- [index.html](d:/javalabs/app/src/main/resources/static/index.html)

Основная логика интерфейса:
- [app.js](d:/javalabs/app/src/main/resources/static/app.js)

Стили интерфейса:
- [app.css](d:/javalabs/app/src/main/resources/static/app.css)

Как это работает:
- Spring Boot раздает `index.html`, `app.js` и `app.css` как статические файлы
- React монтируется в `<div id="root"></div>`
- весь интерфейс рендерит компонент `App()`

## Верхняя часть страницы

Что видит пользователь:
- заголовок страницы
- короткое описание сервиса

Где реализовано:
- разметка: [app.js](d:/javalabs/app/src/main/resources/static/app.js)
  `section.hero hero-simple`
- стили: [app.css](d:/javalabs/app/src/main/resources/static/app.css)
  `.hero`, `.hero-card`, `.hero-simple`, `.eyebrow`

Как работает:
- это обычный JSX-блок в `return (...)` внутри `App()`
- логики у него почти нет, это визуальная шапка

## Вкладки "Рестораны / Клиенты / Бронирования"

Что видит пользователь:
- переключатели разделов

Где реализовано:
- [app.js](d:/javalabs/app/src/main/resources/static/app.js)
  массив:
  ```js
  [
    ["restaurants", "Рестораны"],
    ["customers", "Клиенты"],
    ["bookings", "Бронирования"]
  ]
  ```
- стили: [app.css](d:/javalabs/app/src/main/resources/static/app.css)
  `.nav-tabs`, `.tab`, `.tab.active`

Как работает:
- состояние вкладки хранится в `const [tab, setTab] = useState("restaurants")`
- при нажатии вызывается `setTab(value)`
- ниже в JSX есть условия:
  - `tab === "restaurants"`
  - `tab === "customers"`
  - `tab === "bookings"`
- в зависимости от значения рисуется нужный раздел

## Всплывающие уведомления

Что видит пользователь:
- сообщения `ГОТОВО`
- сообщения `ОШИБКА`

Где реализовано:
- логика: [app.js](d:/javalabs/app/src/main/resources/static/app.js)
  состояния:
  - `message`
  - `error`
- автоматическое скрытие:
  - `useEffect(... [message])`
  - `useEffect(... [error])`
- стили: [app.css](d:/javalabs/app/src/main/resources/static/app.css)
  - `.notice-stack`
  - `.notice-floating`
  - `.notice-title`
  - `.notice-body`
  - `.notice.error`

Как работает:
- если в коде вызывается `setMessage("...")`, показывается успешное уведомление
- если вызывается `setError("...")`, показывается ошибка
- через несколько секунд уведомление исчезает автоматически

## Раздел "Рестораны"

Что видит пользователь:
- слева фильтры
- справа список ресторанов
- кнопку `Добавить ресторан`
- у карточки ресторана кнопки `Изменить` и `Удалить`

### Фильтры

Где реализовано:
- состояние фильтров:
  [app.js](d:/javalabs/app/src/main/resources/static/app.js)
  `const [searchForm, setSearchForm] = useState(...)`
- вычисление отфильтрованного списка:
  [app.js](d:/javalabs/app/src/main/resources/static/app.js)
  `const visibleRestaurants = useMemo(() => { ... })`

Как работает:
- пользователь вводит текст в поля
- `onChange` обновляет `searchForm`
- `useMemo(...)` пересчитывает `visibleRestaurants`
- список справа меняется сразу, без кнопки "Показать"

Почему фильтрация динамическая:
- она зависит от `restaurants` и `searchForm`
- как только меняется `searchForm`, React заново считает видимый список

### Кнопка "Добавить ресторан"

Где реализовано:
- кнопка: [app.js](d:/javalabs/app/src/main/resources/static/app.js)
  `onClick={openCreateRestaurant}`
- функция открытия:
  `openCreateRestaurant()`
- модальное окно:
  `Modal open={restaurantModalOpen} ...`

Как работает:
- по нажатию:
  - сбрасывается `editingRestaurantId`
  - создается пустая форма через `defaultRestaurantForm()`
  - открывается modal через `setRestaurantModalOpen(true)`

### Кнопка "Изменить"

Где реализовано:
- кнопка в карточке ресторана:
  `onClick={() => openEditRestaurant(restaurant)}`
- функция:
  `openEditRestaurant(restaurant)`

Как работает:
- берет текущий ресторан
- преобразует его в форму через `serializeRestaurant(restaurant)`
- открывает то же модальное окно, но уже в режиме редактирования

### Кнопка "Удалить"

Где реализовано:
- кнопка в карточке ресторана:
  `onClick={() => removeRestaurant(restaurant.id)}`
- запрос на backend:
  `api.deleteRestaurant(id)`

Как работает:
- отправляется `DELETE` на `/api/v1/restaurants/{id}`
- после успешного ответа вызывается `bootstrap(true)`
- список ресторанов обновляется

### Что внутри карточки ресторана

Что видит пользователь:
- название
- город
- кухня
- удобства
- список столов
- меню

Где реализовано:
- [app.js](d:/javalabs/app/src/main/resources/static/app.js)
  блок `visibleRestaurants.map((restaurant) => (...))`

Как работает:
- данные приходят из backend через `api.getRestaurants()`
- они кладутся в `restaurants`
- каждая карточка рендерится через `.map(...)`

## Форма ресторана

Что видит пользователь:
- поля:
  - название
  - город
  - кухня
  - удобства
  - столы
  - меню

Где реализовано:
- состояние формы:
  `const [restaurantForm, setRestaurantForm] = useState(defaultRestaurantForm())`
- submit:
  `submitRestaurant(event)`
- подготовка payload:
  `parseRestaurantPayload(form)`
- проверки:
  `validateRestaurantForm(form)`

Как работает:
- форма хранится целиком в `restaurantForm`
- поля обновляются через `setRestaurantForm(...)`
- при отправке:
  1. выполняется фронтовая валидация
  2. выполняется проверка на дубли столов и блюд
  3. создается payload
  4. вызывается либо `api.createRestaurant(...)`, либо `api.updateRestaurant(...)`

### Кнопка "+ Добавить стол"

Где реализовано:
- [app.js](d:/javalabs/app/src/main/resources/static/app.js)

Как работает:
- в массив `restaurantForm.tables` добавляется новый объект
  `{ tableNumber: "", seats: "" }`
- после этого React рисует еще одну строку ввода

### Кнопка "+ Добавить блюдо"

Где реализовано:
- [app.js](d:/javalabs/app/src/main/resources/static/app.js)

Как работает:
- в массив `restaurantForm.dishes` добавляется новый объект
  `{ name: "", price: "" }`

## Раздел "Клиенты"

Что видит пользователь:
- список клиентов
- кнопку `Добавить клиента`
- кнопку `Загрузить список`
- кнопку `Удалить` у клиента

### Кнопка "Добавить клиента"

Где реализовано:
- кнопка:
  `onClick={() => setCustomerModalOpen(true)}`
- modal:
  `Modal open={customerModalOpen} ...`
- submit:
  `submitCustomer(event)`

Как работает:
- открывается отдельное окно
- при сохранении вызывается `api.createCustomer(customerForm)`

### Кнопка "Загрузить список"

Где реализовано:
- кнопка:
  `onClick={() => setBulkModalOpen(true)}`
- modal:
  `Modal open={bulkModalOpen} ...`
- submit:
  `submitBulkImport(event)`

Как работает:
- пользователь вставляет несколько клиентов текстом
- текст парсится функцией `parseBulkCustomers(text)`
- затем отправляется на async endpoint:
  `api.startCustomerImport(...)`

### Блок статуса загрузки

Что видит пользователь:
- статус фоновой операции
- сколько уже обработано записей
- кнопку `Обновить статус`

Где реализовано:
- состояние:
  `const [taskStatus, setTaskStatus] = useState(null)`
- обновление:
  `refreshTaskStatus()`

Как работает:
- после запуска async bulk backend возвращает `taskId`
- фронт сохраняет ответ в `taskStatus`
- потом можно запрашивать статус еще раз по `taskId`

### Кнопка "Удалить" у клиента

Где реализовано:
- [app.js](d:/javalabs/app/src/main/resources/static/app.js)
  `removeCustomer(id, fullName)`
- backend endpoint:
  [CustomerController.java](d:/javalabs/app/src/main/java/com/restaurant/app/controller/customer/CustomerController.java)

Как работает:
- фронт вызывает `api.deleteCustomer(id)`
- backend удаляет клиента, если у него нет связанных броней
- если брони есть, backend возвращает конфликт, а фронт показывает понятное сообщение

## Раздел "Бронирования"

Что видит пользователь:
- список активных броней
- кнопку `Новая бронь`
- кнопку `Отменить`

### Кнопка "Новая бронь"

Где реализовано:
- кнопка:
  `onClick={() => setBookingModalOpen(true)}`
- modal:
  `Modal open={bookingModalOpen} ...`
- submit:
  `submitBooking(event)`

Как работает:
- открывается форма брони
- пользователь выбирает:
  - дату и время
  - количество мест
  - клиента
  - стол
- затем вызывается `api.createBooking(...)`

### Поле "Сколько мест"

Где реализовано:
- состояние:
  `bookingForm.guestCount`
- проверки:
  - `validateBookingForm(form)`
  - `validateBookingCapacity(form, allTables)`

Как работает:
- фронт сначала проверяет, что введено целое число больше нуля
- затем сравнивает это число с вместимостью выбранного стола
- если пользователь просит больше мест, чем есть у стола, запрос даже не отправляется
- если мест у стола достаточно, но на это время они уже заняты, ошибку вернет backend

### Кнопка "Отменить"

Где реализовано:
- [app.js](d:/javalabs/app/src/main/resources/static/app.js)
  `updateBookingStatus(id, "CANCELED")`

Как работает:
- фронт вызывает backend на изменение статуса брони
- после обновления список перезагружается
- в интерфейсе показываются только активные брони:
  `bookings.filter((booking) => booking.status !== "CANCELED")`

## Модальные окна

Что видит пользователь:
- отдельные окна для создания или изменения данных

Где реализовано:
- компонент:
  [app.js](d:/javalabs/app/src/main/resources/static/app.js)
  `function Modal({ open, title, onClose, children })`
- стили:
  [app.css](d:/javalabs/app/src/main/resources/static/app.css)
  - `.modal-backdrop`
  - `.modal-card`
  - `.modal-head`

Как работает:
- если `open === false`, компонент возвращает `null`
- если `open === true`, рисуется затемнение и карточка окна
- клик по фону закрывает окно
- клик внутри окна не закрывает его

## Откуда вообще берутся данные

Где реализовано:
- [app.js](d:/javalabs/app/src/main/resources/static/app.js)
  объект `api`

Что там есть:
- `getRestaurants()`
- `createRestaurant(...)`
- `updateRestaurant(...)`
- `deleteRestaurant(...)`
- `getCustomers()`
- `createCustomer(...)`
- `deleteCustomer(...)`
- `startCustomerImport(...)`
- `getImportStatus(...)`
- `getBookings()`
- `createBooking(...)`
- `updateBookingStatus(...)`

Как работает:
- это обертка над `fetch(...)`
- все запросы идут в backend по `/api/v1/...`
- если backend вернул ошибку, `api.request(...)` пытается вытащить сообщение и бросает `Error`

## Как обновляется экран после действий

Главная функция:
- [app.js](d:/javalabs/app/src/main/resources/static/app.js)
  `bootstrap(silent = false)`

Что она делает:
- загружает рестораны
- загружает клиентов
- загружает брони
- сохраняет все это в state

Как вызывается:
- при первом открытии страницы через `useEffect(...)`
- после создания, удаления, обновления данных

## Какие `useState` здесь самые важные

Основные:
- `tab` — какая вкладка сейчас открыта
- `restaurants` — список ресторанов
- `customers` — список клиентов
- `bookings` — список броней
- `message` — успешное уведомление
- `error` — ошибка
- `restaurantForm` — форма ресторана
- `customerForm` — форма клиента
- `bulkForm` — форма bulk-загрузки
- `bookingForm` — форма брони
- `taskStatus` — статус async задачи

## Как кратко объяснить фронт на защите

Короткая версия:
- `index.html` подключает React, ReactDOM, Babel, CSS и основной `app.js`
- весь интерфейс строится в компоненте `App()`
- состояние экрана хранится через `useState`
- данные с backend загружаются через `fetch` в объекте `api`
- вкладки, формы, модальные окна, фильтры и уведомления рендерятся условно в JSX
- после действий пользователя данные заново подтягиваются через `bootstrap()`
