## Добавляем запрос вещи
В этом спринте разработка будет вестись в ветке add-item-requests. Начнём с новой функциональности — с запросов на добавление вещи. Её суть в следующем.

Пользователь создаёт такой запрос, когда не может найти нужную вещь, воспользовавшись поиском, но при этом надеется, что у кого-то она всё же имеется. Другие пользователи могут просматривать подобные запросы и, если у них есть описанная вещь и они готовы предоставить её в аренду, добавлять нужную вещь в ответ на запрос.

Таким образом, вам нужно добавить четыре новых эндпоинта:

1. POST /requests — добавить новый запрос вещи. Основная часть запроса — текст запроса, где пользователь описывает, какая именно вещь ему нужна.
2. GET /requests — получить список своих запросов вместе с данными об ответах на них. Для каждого запроса должны указываться описание, дата и время создания и список ответов в формате: id вещи, название, её описание description, а также requestId запроса и признак доступности вещи available. Так в дальнейшем, используя указанные id вещей, можно будет получить подробную информацию о каждой вещи. Запросы должны возвращаться в отсортированном порядке от более новых к более старым.
3. GET /requests/all?from={from}&size={size} — получить список запросов, созданных другими пользователями. С помощью этого эндпоинта пользователи смогут просматривать существующие запросы, на которые они могли бы ответить. Запросы сортируются по дате создания: от более новых к более старым. Результаты должны возвращаться постранично. Для этого нужно передать два параметра: from — индекс первого элемента, начиная с 0, и size — количество элементов для отображения.
4. GET /requests/{requestId} — получить данные об одном конкретном запросе вместе с данными об ответах на него в том же формате, что и в эндпоинте GET /requests. Посмотреть данные об отдельном запросе может любой пользователь.

## Добавляем опцию ответа на запрос
Добавим ещё одну полезную опцию в ваше приложение, чтобы пользователи могли отвечать на запросы друг друга. Для этого при создании вещи должна быть возможность указать id запроса, в ответ на который создаётся нужная вещь.

Добавьте поле requestId в тело запроса POST /items. Обратите внимание, что должна сохраниться возможность добавить вещь и без указания requestId.

Реализуйте вышеперечисленные эндпоинты. Если у вас возникнут трудности, можете воспользоваться советами в прикреплённом файле.

## Добавляем пагинацию к существующим эндпоинтам
Теперь вернёмся к улучшению, о котором мы упомянули ранее. Вы уже используете в запросе GET /requests/all пагинацию, поскольку запросов может быть очень много.

Пользователи уже жалуются, что запросы возвращают слишком много данных и с ними невозможно работать. Эта проблема возникает при просмотре бронирований и особенно при просмотре вещей. Поэтому, чтобы приложение было комфортным для пользователей, а также быстро работало, вам предстоит добавить пагинацию в эндпоинты GET /items, GET /items/search, GET /bookings и GET /bookings/owner.

Параметры будут такими же, как и для эндпоинта на получение запросов вещей: номер первой записи и желаемое количество элементов для отображения.

## Добавляем тесты
И наконец, ещё одна очень важная задача этого спринта — написать тесты для приложения ShareIt. Не оставляйте эту задачу на конец работы. Делайте всё постепенно: перед тем как реализовать какую-либо часть задания, сформулируйте функциональные и нефункциональные требования к ней. В соответствии с этими требованиями напишите реализацию, после этого напишите юнит-тесты, проверяющие реализацию на соответствие требованиям.

После того как будут написаны тесты для новой функциональности, описанной в этом техзадании, перейдите к написанию тестов к тому, что было реализовано в предыдущих спринтах. В реальной практике программисты пишут тесты параллельно с новым кодом. Так каждая функция, которую они разрабатывают, изначально покрывается тестами.

При написании тестов вам предстоит решить несколько задач:

1. Реализовать юнит-тесты для всего кода, содержащего логику. Выберите те классы, которые содержат в себе нетривиальные методы, условия и ветвления. В основном это будут классы сервисов. Напишите юнит-тесты на все такие методы, используя моки при необходимости.
2. Реализовать интеграционные тесты, проверяющие взаимодействие с базой данных. Как вы помните, интеграционные тесты представляют собой более высокий уровень тестирования: их обычно требуется меньше, но покрытие каждого — больше. Мы предлагаем вам создать по одному интеграционному тесту для каждого крупного метода в ваших сервисах. Например, для метода getUserItems в классе ItemServiceImpl.
3. Реализовать тесты для REST-эндпоинтов вашего приложения с использованием MockMVC. Вам нужно покрыть тестами все существующие эндпоинты. При этом для слоя сервисов используйте моки.
4. Реализовать тесты для слоя репозиториев вашего приложения с использованием аннотации @DataJpaTest. Есть смысл написать тесты для тех репозиториев, которые содержат кастомные запросы. Работа с аннотацией @DataJpaTest не рассматривалась подробно в уроке, поэтому вам предстоит изучить пример самостоятельно, перейдя по ссылке. Ещё больше деталей вы сможете найти в приложенном файле с советами ментора.
5. Реализовать тесты для работы с JSON для DTO в вашем приложении с помощью аннотации @JsonTest. Такие тесты имеют смысл в тех случаях, когда ваши DTO содержат в себе некоторую логику. Например, описание формата дат или валидацию. Выберите DTO, где есть подобные условия, и напишите тесты.