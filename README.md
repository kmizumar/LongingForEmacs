# LongingForEmacs
IntelliJ IDEA LivePlugin plugin for Emacs Lovers

## What's this
This is a small groovy script used with Dmitry Kandalov's LivePlugin for IntelliJ IDEA (see https://github.com/dkandalov/live-plugin) to provide some editing behavior for Emacs Lovers.

## Why?
When I start using IntelliJ IDEA **seriously**, I mean, start writing my code in IntelliJ, I found it's really frustrating since some editing command which I heavily uses on Emacs are not supported. Changing Keymaps to Emacs is not enough, so I made this to make me happy.

## Available commands
* transpose-chars
* transpose-words

### transpose-chars
Initially mapped to CTRL-T, and works like Emacs Lisp version of transpose-chars function without ARG. I know there was a well-known workaround which uses "Swap Characters" action from "String Manipulation" plugin, but its behavior is not what I want (it does nothing when the point is at the first character of the line and at the end of line). If you know how to pass ARG to IntelliJ's actions, please let me know.

### transpose-words
Initially mapped to META-T, and works like Emacs Lisp version of transpose-words function without ARG. If you know how to pass ARG to IntelliJ's actions, please let me know.
