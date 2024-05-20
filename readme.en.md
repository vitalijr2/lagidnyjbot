# Lagidnyj Bot

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/b9b50b8488734a498b84a47488d6b89f)](https://app.codacy.com/gh/vitalijr2/lagidnyjbot/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Codacy Coverage](https://app.codacy.com/project/badge/Coverage/b9b50b8488734a498b84a47488d6b89f)](https://app.codacy.com/gh/vitalijr2/lagidnyjbot/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_coverage)
[![Java Version](https://img.shields.io/static/v1?label=java&message=17&color=blue&logo=java&logoColor=E23D28)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)

[![Українська](https://img.shields.io/badge/%F0%9F%93%84-%D0%A3%D0%BA%D1%80%D0%B0%D1%97%D0%BD%D1%81%D1%8C%D0%BA%D0%BE%D1%8E-blue)](readme.uk.md)
[![Latynka](https://img.shields.io/badge/%F0%9F%93%84-Latynka-blue)](readme.md)

## Restrictions on using of Russian in a Telegram chat.

If:

* you want conversation in your chat to be mainly in Ukrainian, or are you annoyed by Russian,
* Russian trolls sometimes run into your chat and the moderators don't have time to ban them, or you want to automate
  this process

— then Telegram bot [@lagidnyjbot][bot] is just for you.

## How to use the bot?

Add a bot to your chat and give it chat moderator rights. As a moderator, he only needs one permission: to restrict chat
visitors.

The bot will read all messages and if it finds the letters **ё**, **ъ**, **ы** or **э** in them, it will first warn the
author of the
messages about the rules of the chat, and then switch the person to "read-only" mode.

What to do with the violator next is up to you. I draw your attention to the fact that messages in the Russian language
are not deleted.

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

[bot]: https://t.me/lagidnyjbot "лагідна українізація"

[license]: http://www.apache.org/licenses/LICENSE-2.0 "Apache License, Version 2.0"
