# sport-geolocation-game
Геолокационная командная игра для Android. Здесь представлена только Android часть. Исходный код сервера никуда не выложен.

ТЗ по этой ссылке:
https://docs.google.com/document/d/1LYJfkWtrhvbSMGvVD85nJVDQN9rcoUmTAjwOJI7pfPc/edit?usp=sharing


## Правила игры:

**Цель игры** - собрать как можно больше флажков за определённое время.

**Подготовка к игре:**  
Все игроки собираются вместе и делятся на команды по цветам. На карте генерируются случайные флажки с цветом команд. Для каждой команды по несколько флажков. Флажки пока полупрозрачные, так как не активированы.

У каждой команды есть своя стартовая энергия. Она будет пополняться с определённой скоростью.

**Процесс игры:**  
Игроки ходят по карте и собирают флажки своего цвета. Также они могут собирать бонусы в виде энергии, случайно появляющиеся по пути.

Чтобы флажок считался собранным, надо активировать его. Для этого надо нажать на флажок. На это тратится энергия всей команды. Чем больше расстояние до флажка - тем больше энергии тратится. Как только у команды появился новый флажок - скорость пополнения ее энергии увеличивается.

Также игроки могут захватывать флаги чужой команды. На это тратится больше энергии. Если флажок не активирован - в 2 раза больше, если активирован - то в 4.

**Конец игры:**  
Игра заканчивается когда все флажки захвачены одной командой или закончилось время игры. Победителем становится та команда, у которой больше захваченных флажков. Если флажков у всех поровну, побеждает та команда, у которой больше энергии.
