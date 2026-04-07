# Фронтенд в проекте: объяснение для бэкендера

## Что это вообще за фронт

В проект добавлен простой SPA-клиент на React, но без отдельной сборки через `Vite` или `Webpack`.

Файлы фронта:

- [index.html](/d:/javalabs/app/src/main/resources/static/index.html)
- [app.js](/d:/javalabs/app/src/main/resources/static/app.js)
- [app.css](/d:/javalabs/app/src/main/resources/static/app.css)

Так как они лежат в `src/main/resources/static`, Spring Boot сам раздаёт их как статику. Поэтому фронт открывается на том же порту, что и backend:

- `http://localhost:8080/`

То есть здесь нет отдельного frontend-сервера. Один и тот же Spring Boot:

- отдаёт HTML/CSS/JS
- принимает REST-запросы на `/api/v1/...`

## Почему это React, хотя тут просто HTML, JS и CSS

Потому что React можно подключить не только через отдельный frontend-проект, но и прямо в HTML.

В [index.html](/d:/javalabs/app/src/main/resources/static/index.html):

- подключается `React`
- подключается `ReactDOM`
- подключается `Babel`
- создаётся контейнер `div` с `id="root"`

React-приложение потом монтируется в этот контейнер через:

```javascript
ReactDOM.createRoot(document.getElementById("root")).render(<App />);
```

Это означает:

- найти на странице `div#root`
- создать в нём корень React-приложения
- отрисовать компонент `App`

То есть реальный UI строит уже React, а не сам HTML.

## Роль каждого файла

### 1. `index.html`

Это точка входа.

Он:

- подключает стили
- подключает React-библиотеки
- подключает `app.js`
- содержит пустой контейнер:

```html
<div id="root"></div>
```

Сам интерфейс здесь почти не описан.

### 2. `app.js`

Это основная логика фронта.

В нём:

- React-компоненты
- состояние страницы
- обработчики кнопок и форм
- HTTP-запросы к backend
- рендер вкладок и карточек

### 3. `app.css`

Это только оформление:

- цвета
- отступы
- сетка
- кнопки
- карточки

Бизнес-логики там нет.

## Как фронт связан с backend

Связь идёт через обычные HTTP-запросы `fetch`.

В [app.js](/d:/javalabs/app/src/main/resources/static/app.js) есть объект `api`, внутри которого описаны методы:

```javascript
const api = {
  getRestaurants() { ... },
  createRestaurant(payload) { ... },
  updateRestaurant(id, payload) { ... },
  deleteRestaurant(id) { ... },
  getCustomers() { ... },
  createCustomer(payload) { ... }
};
```

Каждый такой метод вызывает:

```javascript
fetch(...)
```

Например:

- `GET /api/v1/restaurants/all`
- `POST /api/v1/restaurants`
- `PUT /api/v1/restaurants/{id}`
- `DELETE /api/v1/restaurants/{id}`
- `GET /api/v1/restaurants/search/jpql`
- `GET /api/v1/restaurants/search/native`

То есть frontend не работает с базой напрямую. Он общается только с REST API.

## Как идёт жизненный цикл данных

### Пример: загрузка данных при открытии страницы

При старте React вызывает:

```javascript
useEffect(() => {
  bootstrap();
}, []);
```

Это значит:

- после первого рендера страницы вызвать `bootstrap()`
- пустой массив `[]` означает "только один раз при первом открытии"

`bootstrap()` делает несколько запросов:

- получить рестораны
- получить клиентов
- получить брони

И сохраняет это в состояние через `setRestaurants`, `setCustomers`, `setBookings`.

После изменения состояния React автоматически перерисовывает интерфейс.

### Пример: создание ресторана

Когда пользователь отправляет форму, вызывается:

```javascript
async function submitRestaurant(event) { ... }
```

Что происходит:

1. `event.preventDefault()` запрещает обычную отправку HTML-формы.
2. Из полей формы собирается JSON.
3. Вызывается `api.createRestaurant(payload)` или `api.updateRestaurant(...)`.
4. Backend возвращает DTO.
5. Потом снова вызывается `bootstrap()`, чтобы обновить данные на странице.

То есть схема такая:

`форма -> fetch -> backend -> JSON -> setState -> React перерисовал UI`

## Почему это SPA

SPA означает, что:

- у пользователя одна HTML-страница
- интерфейс меняется внутри неё
- данные догружаются через API
- нет серверной навигации между разными страницами

У тебя это выполняется:

- открывается один `index.html`
- вкладки `Restaurants / Customers / Bookings` переключаются внутри React
- CRUD и фильтрация идут через `fetch`
- сервер не рендерит отдельные HTML-страницы под каждое действие

Важно:

если ты изменил данные через Postman, а фронт сам об этом не узнал, это не ломает SPA. Это просто значит, что у клиента нет realtime-синхронизации с внешними изменениями.

## Как здесь отображаются связи сущностей

### OneToMany

Для ресторана показываются:

- `tables`
- `dishes`

Это приходит в `RestaurantDto` как списки.

Во фронте это просто рендерится через `.map(...)`, например:

```javascript
(restaurant.tables || []).map((table) => ...)
```

Смысл:

- взять массив `tables`
- пройти по каждому элементу
- вернуть JSX для каждого элемента

### ManyToMany

Для ресторана также показываются `amenities`.

С backend-стороны:

- один ресторан может иметь много amenities
- одна amenity может принадлежать многим ресторанам

Во фронте это такой же массив строк, который просто выводится списком.

## Базовый синтаксис JavaScript в этом файле

Ниже самое важное, что стоит понимать.

### `const`

```javascript
const api = { ... };
```

Это объявление константы. Аналог "ссылка на объект, которую не будут переназначать".

### Функция

```javascript
function bootstrap() { ... }
```

Обычная именованная функция.

### Стрелочная функция

```javascript
(restaurant) => restaurant.name
```

Это короткая запись функции.

Почти то же самое, что:

```javascript
function (restaurant) {
  return restaurant.name;
}
```

### Объект

```javascript
{
  name: form.name.trim(),
  city: form.city.trim()
}
```

Это JS-объект. По смыслу близко к DTO.

### Массив

```javascript
["restaurants", "customers", "bookings"]
```

Это список значений.

### `.map(...)`

```javascript
restaurants.map((restaurant) => ...)
```

Это проход по массиву с преобразованием каждого элемента.

Во фронте очень часто используется для построения списка карточек.

### `.filter(...)`

```javascript
.filter(Boolean)
```

Оставляет только подходящие элементы.

Например, убрать пустые строки.

### `async/await`

```javascript
async function bootstrap() {
  const data = await api.getRestaurants();
}
```

Это удобная запись асинхронного кода.

По смыслу:

- `async` говорит, что внутри будут асинхронные операции
- `await` говорит "дождись результата"

Для бэкендера это удобнее всего воспринимать как неблокирующее ожидание результата Promise.

### `try/catch`

```javascript
try {
  ...
} catch (error) {
  setError(error.message);
}
```

Это обработка ошибок.

Если запрос к backend упал, сообщение выводится в UI.

## Что такое JSX

В `app.js` ты видишь конструкции вида:

```javascript
<div className="card">
  <h3>{restaurant.name}</h3>
</div>
```

Это не чистый JavaScript и не обычный HTML. Это JSX.

JSX:

- похож на HTML
- пишется внутри JavaScript
- потом Babel преобразует его в вызовы React

Почему это работает:

- в `index.html` подключён Babel
- он на лету преобразует JSX в обычный JavaScript для React

## Что такое React state

Вверху `App()` есть конструкции:

```javascript
const [restaurants, setRestaurants] = useState([]);
```

Это состояние компонента.

Смысл:

- `restaurants` — текущее значение
- `setRestaurants(...)` — способ его изменить

Когда вызывается `setRestaurants(...)`, React понимает, что данные изменились, и перерисовывает UI.

Для бэкендера можно думать об этом как о внутреннем in-memory состоянии страницы.

## Что делает `useEffect`

```javascript
useEffect(() => {
  bootstrap();
}, []);
```

Это "выполни побочный эффект после рендера".

Здесь побочный эффект — загрузка данных с сервера.

Пустой массив зависимостей `[]` значит:

- выполнить один раз после первой отрисовки

## Что делает `useMemo`

```javascript
const restaurantStats = useMemo(() => ({ ... }), [restaurants]);
```

Это вычисляемое значение, зависящее от `restaurants`.

Здесь оно считает:

- сколько ресторанов
- сколько столов
- сколько блюд
- сколько amenities

То есть это не отдельные данные из backend, а производное значение из уже загруженного состояния.

## Как устроены формы

Каждая форма связана с состоянием.

Пример:

```javascript
<input name="city" value={restaurantForm.city} onChange={handleRestaurantFormChange} />
```

Что это значит:

- значение поля берётся из `restaurantForm.city`
- при изменении поля вызывается обработчик
- обработчик обновляет state

Это называется controlled component: поле управляется React-состоянием.

## Почему после Postman иногда нужен refresh

Потому что фронт узнаёт об изменениях только когда сам делает запрос.

Если данные изменились вне клиента:

- через Postman
- через другой браузер
- через SQL

текущий SPA сам об этом не узнаёт.

Поэтому нужен:

- reload страницы
- или ручной запрос
- или polling/WebSocket

Это нормально и не противоречит SPA.

## Как это объяснить преподавателю коротко

Короткая версия:

1. Фронт лежит в `static`, поэтому Spring Boot сам его раздаёт.
2. `index.html` только подключает React и контейнер.
3. Весь UI и логика находятся в `app.js`.
4. React хранит состояние страницы через `useState`.
5. Данные приходят с backend через `fetch` на `/api/v1/...`.
6. После получения JSON React обновляет состояние и перерисовывает интерфейс.
7. `tables` и `dishes` показывают `OneToMany`, `amenities` показывают `ManyToMany`.

## Что смотреть в коде в первую очередь

Если хочешь быстро понять фронт, читай в таком порядке:

1. [index.html](/d:/javalabs/app/src/main/resources/static/index.html)
2. [app.js](/d:/javalabs/app/src/main/resources/static/app.js)
3. блок `const api = { ... }`
4. компонент `App()`
5. `bootstrap()`
6. `submitRestaurant()`, `createCustomer()`, `createBooking()`
7. JSX-блоки вкладок `restaurants`, `customers`, `bookings`
8. [app.css](/d:/javalabs/app/src/main/resources/static/app.css)

## Итог

Этот фронт — это не "настоящий отдельный frontend-проект", а встроенный React SPA поверх существующего Spring Boot backend.

Для лабораторной это удобно, потому что:

- не нужен отдельный Node.js-проект
- не нужен отдельный frontend server
- всё запускается вместе с backend
- можно показать CRUD, фильтрацию и связи сущностей на одном экране
