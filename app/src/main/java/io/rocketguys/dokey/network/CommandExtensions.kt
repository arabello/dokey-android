package io.rocketguys.dokey.network

import model.command.Command

fun Command.isAppOpen() : Boolean {
    return this.category == "ao"
}