package com.jawaadianinc.valorant_stats.valo.classes

class eventsClass(
    killerX: Int,
    killerY: Int,
    victimX: Int,
    victimY: Int,
    time: Int,
    killerTeam: String,
    victimTeam: String,
    killerProfilePic: String,
    victimProfilePic: String,
    killerWeapon: String,
    victimWeapon: String
) {
    // this class will contain game events such as kills, deaths and spike plants
    // we need to store the coordinates of victim and killer, the time, the player teams, their profile picture and their weapons
    var killerX = killerX
    var killerY = killerY
    var victimX = victimX
    var victimY = victimY
    var time = time
    var killerTeam = killerTeam
    var victimTeam = victimTeam
    var killerProfilePic = killerProfilePic
    var victimProfilePic = victimProfilePic
    var killerWeapon = killerWeapon
    var victimWeapon = victimWeapon
}
