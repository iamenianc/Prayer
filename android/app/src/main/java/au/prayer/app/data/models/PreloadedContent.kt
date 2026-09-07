package au.prayer.app.data.models

object PreloadedContent {

    val HISTORIC_ENTITY_ID = "historic-reformed-prayers"

    fun getHistoricEntity(): IndividualEntity {
        return IndividualEntity(
            id = HISTORIC_ENTITY_ID,
            rootCode = RootCode.GENERAL,
            displayName = "Historic Prayers",
            contextDescription = "Classic Reformed and historic Anglican BCP prayers",
            isPreloadedHistoric = true,
            interactedCount = 0,
            lastInteractedAt = null,
            createdAt = 1700000000000L
        )
    }

    fun getHistoricPrayerPoints(): List<PrayerPoint> {
        return listOf(
            PrayerPoint(
                id = "historic-lords-prayer",
                entityId = HISTORIC_ENTITY_ID,
                title = "The Lord's Prayer",
                description = "Our Father, which art in heaven, Hallowed be thy Name. Thy kingdom come. Thy will be done in earth, As it is in heaven. Give us this day our daily bread. And forgive us our trespasses, As we forgive them that trespass against us. And lead us not into temptation; But deliver us from evil: For thine is the kingdom, The power, and the glory, For ever and ever. Amen.",
                status = PrayerStatus.HISTORIC,
                createdAt = 1700000000000L
            ),
            PrayerPoint(
                id = "historic-collect-peace",
                entityId = HISTORIC_ENTITY_ID,
                title = "Collect for Peace",
                description = "O God, who art the author of peace and lover of concord, in knowledge of whom standeth our eternal life, whose service is perfect freedom; Defend us thy humble servants in all assaults of our enemies; that we, surely trusting in thy defence, may not fear the power of any adversaries, through the might of Jesus Christ our Lord. Amen.",
                status = PrayerStatus.HISTORIC,
                createdAt = 1700000000000L
            ),
            PrayerPoint(
                id = "historic-collect-grace",
                entityId = HISTORIC_ENTITY_ID,
                title = "Collect for Grace",
                description = "O Lord, our heavenly Father, Almighty and everlasting God, who hast safely brought us to the beginning of this day; Defend us in the same with thy mighty power; and grant that this day we fall into no sin, neither run into any kind of danger; but that all our doings may be ordered by thy governance, to do always that is righteous in thy sight; through Jesus Christ our Lord. Amen.",
                status = PrayerStatus.HISTORIC,
                createdAt = 1700000000000L
            ),
            PrayerPoint(
                id = "historic-collect-purity",
                entityId = HISTORIC_ENTITY_ID,
                title = "Collect for Purity",
                description = "Almighty God, unto whom all hearts be open, all desires known, and from whom no secrets are hid; Cleanse the thoughts of our hearts by the inspiration of thy Holy Spirit, that we may perfectly love thee, and worthily magnify thy holy Name; through Christ our Lord. Amen.",
                status = PrayerStatus.HISTORIC,
                createdAt = 1700000000000L
            ),
            PrayerPoint(
                id = "historic-apostles-creed",
                entityId = HISTORIC_ENTITY_ID,
                title = "The Apostles' Creed",
                description = "I believe in God the Father Almighty, Maker of heaven and earth: And in Jesus Christ his only Son our Lord, Who was conceived by the Holy Ghost, Born of the Virgin Mary, Suffered under Pontius Pilate, Was crucified, dead, and buried: He descended into hell; The third day he rose again from the dead; He ascended into heaven, And sitteth on the right hand of God the Father Almighty; From thence he shall come to judge the quick and the dead. I believe in the Holy Ghost; The holy Catholick Church; The Communion of Saints; The Forgiveness of sins; The Resurrection of the body, And the life everlasting. Amen.",
                status = PrayerStatus.HISTORIC,
                createdAt = 1700000000000L
            )
        )
    }
}
