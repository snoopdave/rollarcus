README.txt
===

This is Rollarcus: my experimental fork of [Apache Roller](https://github.com/apache/roller).

This branch __shiro_not_spring__ replaces Spring Security with Apache Shiro and completely removes Spring from Roller.

Status
---

Shiro is working for Roller authentication via form-based login, but some more
advanced features like LDAP and OpenID support are not yet implemented.

The interesting parts are:

* [https://github.com/snoopdave/rollarcus/blob/shiro_not_spring/app/src/main/resources/shiro.ini](shiro.ini) - The Shiro configuration file.
* [https://github.com/snoopdave/rollarcus/blob/shiro_not_spring/app/src/main/java/org/apache/roller/weblogger/auth/ShiroAuthorizingRealm.java](ShiroAuthorizingRealm.java) - Which plugs Roller user and role management into Shiro.

That's all
---

Want to contribute? Shoot me a PR.
