package com.statsup

enum class Months(private val index: Int, val label: String) {
    January(1, "Gen"),
    February(2, "Feb"),
    March(3, "Mar"),
    April(4, "Apr"),
    May(5, "Mag"),
    June(6, "Giu"),
    July(7, "Lug"),
    August(8, "Ago"),
    September(9, "Set"),
    October(10, "Ott"),
    November(11, "Nov"),
    December(12, "Dic");

    companion object {
        fun labelOf(index: Int) = values().single { it.index == index }.label
    }
}

enum class DayOfWeek(val index: Int, val label: String) {
    Monday(1, "Lun"),
    Tuesday(2, "Mar"),
    Wednesday(3, "Mer"),
    Thursday(4, "Gio"),
    Friday(5, "Ven"),
    Saturday(6, "Sab"),
    Sunday(7, "Dom");
}