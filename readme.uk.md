# Lagidnyj Bot

[![Codacy Badge][codacy-badge]][codacy-badge-link]
[![Codacy Coverage][codacy-coverage]][codacy-coverage-link]  
![Libraries.io dependency status for GitHub repo][dependency-status]
[![Java Version][java-version]][jdk-download]

[![English](https://img.shields.io/badge/%F0%9F%93%84-English-blue)](readme.en.md)
[![Latynka](https://img.shields.io/badge/%F0%9F%93%84-Latynka-blue)](readme.md)

## Обмеження спілкування російською мовою в Телеграм чаті

Якщо:

* ви хочете щоб у вашому чаті спілкування було переважно українською мовою, чи вас дратує російська,
* у ваш чат іноді забігають москальські тролі і модератори не встигають їх відлювлювати, чи ви хочете автоматизувати цей
  процес

— тоді Telegram бот [@lagidnyjbot][bot] саме для вас.

## Як користуватись ботом?

Додайте бота до свого чату та надайте йому права модератора чату. Як модератору йому потрібен лише один дозвіл:
обмежувати відвідувачів чату.

Бот буде читати всі повідомлення і зустрівши в них літери **ё**, **ъ**, **ы** чи **э** спочатку попередить автора
повідомлень про правила чату, а потім переведе людину в режим _"тільки читання"_.

Що робити з порушником правил далі - вже ваша справа. Звертаю вашу увагу на те що повідомлення з російською мовою не
видаляються.

## License

Copyright 2024 Vitalij Berdinskih

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0][license]

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

[Apache License v2.0](LICENSE)  
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0.html)

[codacy-badge]: https://app.codacy.com/project/badge/Grade/b9b50b8488734a498b84a47488d6b89f

[codacy-badge-link]: https://app.codacy.com/gh/vitalijr2/lagidnyjbot/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade

[codacy-coverage]: https://app.codacy.com/project/badge/Coverage/b9b50b8488734a498b84a47488d6b89f

[codacy-coverage-link]: https://app.codacy.com/gh/vitalijr2/lagidnyjbot/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_coverage

[dependency-status]: https://img.shields.io/librariesio/github/vitalijr2/lagidnyjbot

[java-version]: https://img.shields.io/static/v1?label=java&message=17&color=blue&logo=java&logoColor=E23D28

[jdk-download]: https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html

[bot]: https://t.me/lagidnyjbot "лагідна українізація"

[license]: http://www.apache.org/licenses/LICENSE-2.0 "Apache License, Version 2.0"
