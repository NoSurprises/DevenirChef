# DevenirChef

## Функционал с деталями реализации
* [ ] Каталог рецептов, разделенных по сложности (и категории). *ViewGroup с данными из Firebase, фильтрация при поступлении*
* [x] Регистрация пользователей, профиль пользователя. *Firebase Authentication*
* [ ] Повышение уровня игрока, (выполнение заданий). *Создание записей в БД, изменение при выполнении заданий*
* [ ] Оценка блюд других пользователей. *Отдельный Activity с набором данных из БД*
* [ ] “Сдача” блюда (с функцией запостить картинку в соцсетях). *Использование Intent Action для получения фотографии*
### Каталог рецептов. *Данные хранятся в БД*
* [x] Сложность блюда по шкале от 1 до 5
* [ ] Теги:
  *  Завтрак/Обед/Ужин
  *  Европейская/Русская/Азиатская/… кухня
  *  Мясо/Птица/Рыба
  *  Вегетарианское/Веганское..
* [x] В каталоге выбора - название, фото, сложность
* [ ] Категории блюд, Строка поиска
### Страница рецепта. *Отдельные Activity - Recipe Activity для информации о рецепте и Cook Activity для режима готовки*
* [x] Первый экран:
  * Картинка
  * Название
  * Сложность
  * Список ингредиентов
  * Кнопка “Старт!”
* [x] Каждый из шагов: *каждый шаг - отдельный фрагмент*
  * Картинка (если есть)
  * Описание шага
  * Кнопка “Далее”
* [x] Последний экран:
  * Надпись “Поздравляем, блюдо готово! Чтобы получить опыт, загрузите
фото приготовленного блюда”
  * [x] Кнопка загрузки фото
* [x] Возможность перехода между шагами 1...n *(используя ViewPager)*
### Регистрация пользователя 
* [x] При первом открытии (при выходе из аккаунта) приложения
  *  Логотип
  *  Кнопка “Регистрация”
  *  Кнопка “Войти”
* [x] Регистрация:
  * Логин
  * Пароль
  * email
* [x] (альтернативная) Регистрация через google аккаунт
### Профиль пользователя. *Отдельный Activity, доступ к нему можно получить из NavigationDrawer*
* [ ] Аватар (При нажатии на него, можно выбрать другую картинку, так стоит
дефолтная)
* [ ] Шкала уровня (Показан текущий уровень и сколько очков опыта не хватает до
следующего уровня)
* [x] История приготовленных рецептов
### Уровни игрока, повышение уровня. *Реализуется с помощью записей в БД*
* [x] Пять уровней
* [x] Первый уровень пользователь получает при регистрации
* [ ] Доступны только рецепты уровень которых меньше или равен текущему уровню
пользователя
* [x] Можно готовить одно и то же блюдо несколько раз
* [ ] Опыт начисляется - в зависимости от сложности блюда
* [ ] Задания - “Приготовьте в течении 24 часов завтрак, обед и ужин”, “Приготовьте
блюдо азиатской кухни” за них - доп. опыт
