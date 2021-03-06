# Chartographer

От **charta** (*лат.*) или **харта** — одно из названий [папируса](https://ru.wikipedia.org/wiki/Папирус).

**Chartographer** - сервис, предназначенный для восстановления изображений древних свитков и папирусов.
Изображения растровые и создаются поэтапно (отдельными фрагментами).
Восстановленное изображение можно получать фрагментами (даже если оно лишь частично восстановленное).

## HTTP API

Доступно 4 HTTP-метода:

```
POST /chartas/?width={width}&height={height}
```
Создать новое изображение папируса заданного размера (в пикселях),
где `{width}` и `{height}` — положительные целые числа.  
Тело запроса пустое.  
В теле ответа возвращается `{id}` — уникальный идентификатор изображения в строковом представлении.  
Код ответа: `201 Created`.

```
POST /chartas/{id}/?x={x}&y={y}&width={width}&height={height}
```
Сохранить восстановленный фрагмент изображения размера `{width} x {height}` с координатами `({x};{y})`.
Под координатами подразумевается положение левого верхнего угла фрагмента относительно левого верхнего угла всего изображения.
Другими словами, левый верхний угол изображения является началом координат, т.е. эта точка имеет координаты `(0;0)`.  
Тело запроса: изображение в формате `BMP` (цвет в RGB, 24 бита на 1 пиксель).  
Тело ответа пустое.  
Код ответа: `200 OK`.

```
GET /chartas/{id}/?x={x}&y={y}&width={width}&height={height}
```
Получить восстановленную часть изображения размера `{width} x {height}` с координатами `({x};{y})`,
где `{width}` и `{height}` — положительные целые числа.
Под координатами подразумевается положение левого верхнего угла фрагмента относительно левого верхнего угла всего изображения.
Другими словами, левый верхний угол изображения является началом координат, т.е. эта точка имеет координаты `(0;0)`.  
Тело ответа: изображение в формате `BMP` (цвет в RGB, 24 бита на 1 пиксель).  
Код ответа: `200 OK`.

```
DELETE /chartas/{id}/
```
Удалить изображение с идентификатором `{id}`.  
Тело запроса и ответа пустое.  
Код ответа: `200 OK`.

### Обработка ошибок

1. Запросы по `{id}` изображения, которого не существует, завершаются с кодом ответа `404 Not Found`.
2. Запросы с некорректными параметрами `{width}` или `{height}` завершаются с кодом ответа `400 Bad Request`.
3. Запросы с фрагментами, которые не пересекаются по координатам с изображением, завершаются с кодом ответа `400 Bad Request`.
   При этом фрагменты могут *частично* находиться вне границ изображения (см. примечания) — такие запросы считаются корректными.

### Примечания

1. Формат изображений — [BMP](https://ru.wikipedia.org/wiki/BMP). Цветность в RGB (без альфа-канала), 24 бита на пиксель.
2. В случае, если загружаемый восстановленный фрагмент перекрывает восстановленную ранее часть, то в любом случае применяется новый фрагмент.
3. Если запрашивается фрагмент, часть которого ещё не восстановлена, то не восстановленные области закрашиваются чёрным цветом.
   Аналогично, чёрным цветом закрашивается та часть фрагмента, которая оказывается вне границ изображения (см. пример ниже).
4. Если восстанавливаемый фрагмент перекрывает границы изображения, то часть фрагмента вне изображения игнорируется.
   Пример: размер изображения — `50 x 100`, фрагмент с размером `50 x 50` и координатами верхнего левого угла `(25;25)`.
   Правая половина фрагмента игнорируется. Схематически изображено ниже.

```
╔═════════╗
║         ║
║    ┌────╫────┐
║    │    ║    │
║    │    ║    │
║    │    ║    │
║    └────╫────┘
║         ║
╚═════════╝
```