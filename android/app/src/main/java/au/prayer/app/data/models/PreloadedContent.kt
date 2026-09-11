package au.prayer.app.data.models

data class PreloadedHistoricTopic(
    val entity: IndividualEntity,
    val prayerPoint: PrayerPoint
)

object PreloadedContent {

    // Ordered catalog of public domain historic prayers (Spurgeon, 1662 BCP, ecumenical creeds)
    val HISTORIC_TOPICS: List<PreloadedHistoricTopic> = listOf(
        // 1. The Lord's Prayer
        PreloadedHistoricTopic(
            entity = IndividualEntity(
                id = "historic-lords-prayer",
                rootCode = RootCode.GENERAL,
                displayName = "The Lord's Prayer",
                contextDescription = "The model prayer taught by our Lord Jesus Christ (Matthew 6:9–13)",
                isPreloadedHistoric = true,
                createdAt = 1700000001000L
            ),
            prayerPoint = PrayerPoint(
                id = "point-historic-lords-prayer",
                entityId = "historic-lords-prayer",
                title = "The Lord's Prayer",
                description = "Our Father, which art in heaven, Hallowed be thy Name. Thy kingdom come. Thy will be done in earth, As it is in heaven. Give us this day our daily bread. And forgive us our trespasses, As we forgive them that trespass against us. And lead us not into temptation; But deliver us from evil: For thine is the kingdom, The power, and the glory, For ever and ever. Amen.",
                status = PrayerStatus.HISTORIC,
                createdAt = 1700000001000L
            )
        ),
        // 2. 1662 BCP: Collect for Peace
        PreloadedHistoricTopic(
            entity = IndividualEntity(
                id = "historic-collect-peace",
                rootCode = RootCode.GENERAL,
                displayName = "Collect for Peace (1662 BCP)",
                contextDescription = "Historic Anglican collect from the 1662 Book of Common Prayer (Morning Prayer)",
                isPreloadedHistoric = true,
                createdAt = 1700000002000L
            ),
            prayerPoint = PrayerPoint(
                id = "point-historic-collect-peace",
                entityId = "historic-collect-peace",
                title = "Collect for Peace",
                description = "O God, who art the author of peace and lover of concord, in knowledge of whom standeth our eternal life, whose service is perfect freedom; Defend us thy humble servants in all assaults of our enemies; that we, surely trusting in thy defence, may not fear the power of any adversaries, through the might of Jesus Christ our Lord. Amen.",
                status = PrayerStatus.HISTORIC,
                createdAt = 1700000002000L
            )
        ),
        // 3. 1662 BCP: Collect for Grace
        PreloadedHistoricTopic(
            entity = IndividualEntity(
                id = "historic-collect-grace",
                rootCode = RootCode.GENERAL,
                displayName = "Collect for Grace (1662 BCP)",
                contextDescription = "Historic Anglican collect from the 1662 Book of Common Prayer (Morning Prayer)",
                isPreloadedHistoric = true,
                createdAt = 1700000003000L
            ),
            prayerPoint = PrayerPoint(
                id = "point-historic-collect-grace",
                entityId = "historic-collect-grace",
                title = "Collect for Grace",
                description = "O Lord, our heavenly Father, Almighty and everlasting God, who hast safely brought us to the beginning of this day; Defend us in the same with thy mighty power; and grant that this day we fall into no sin, neither run into any kind of danger; but that all our doings may be ordered by thy governance, to do always that is righteous in thy sight; through Jesus Christ our Lord. Amen.",
                status = PrayerStatus.HISTORIC,
                createdAt = 1700000003000L
            )
        ),
        // 4. 1662 BCP: Collect for Purity
        PreloadedHistoricTopic(
            entity = IndividualEntity(
                id = "historic-collect-purity",
                rootCode = RootCode.GENERAL,
                displayName = "Collect for Purity (1662 BCP)",
                contextDescription = "Historic Anglican collect from the 1662 Book of Common Prayer (Holy Communion)",
                isPreloadedHistoric = true,
                createdAt = 1700000004000L
            ),
            prayerPoint = PrayerPoint(
                id = "point-historic-collect-purity",
                entityId = "historic-collect-purity",
                title = "Collect for Purity",
                description = "Almighty God, unto whom all hearts be open, all desires known, and from whom no secrets are hid; Cleanse the thoughts of our hearts by the inspiration of thy Holy Spirit, that we may perfectly love thee, and worthily magnify thy holy Name; through Christ our Lord. Amen.",
                status = PrayerStatus.HISTORIC,
                createdAt = 1700000004000L
            )
        ),
        // 5. 1662 BCP: A General Thanksgiving
        PreloadedHistoricTopic(
            entity = IndividualEntity(
                id = "historic-general-thanksgiving",
                rootCode = RootCode.GENERAL,
                displayName = "A General Thanksgiving (1662 BCP)",
                contextDescription = "Composed by Bishop Edward Reynolds for the 1662 Book of Common Prayer",
                isPreloadedHistoric = true,
                createdAt = 1700000005000L
            ),
            prayerPoint = PrayerPoint(
                id = "point-historic-general-thanksgiving",
                entityId = "historic-general-thanksgiving",
                title = "A General Thanksgiving",
                description = "Almighty God, Father of all mercies, we thine unworthy servants do give thee most humble and hearty thanks for all thy goodness and loving-kindness to us, and to all men. We bless thee for our creation, preservation, and all the blessings of this life; but above all, for thine inestimable love in the redemption of the world by our Lord Jesus Christ; for the means of grace, and for the hope of glory. And, we beseech thee, give us that due sense of all thy mercies, that our hearts may be unfeignedly thankful, and that we shew forth thy praise, not only with our lips, but in our lives; by giving up our selves to thy service, and by walking before thee in holiness and righteousness all our days; through Jesus Christ our Lord, to whom with thee and the Holy Ghost be all honour and glory, world without end. Amen.",
                status = PrayerStatus.HISTORIC,
                createdAt = 1700000005000L
            )
        ),
        // 6. The Apostles' Creed
        PreloadedHistoricTopic(
            entity = IndividualEntity(
                id = "historic-apostles-creed",
                rootCode = RootCode.GENERAL,
                displayName = "The Apostles' Creed",
                contextDescription = "Historic ecumenical confession of the Christian faith (Thirty-Nine Articles, Art. VIII)",
                isPreloadedHistoric = true,
                createdAt = 1700000006000L
            ),
            prayerPoint = PrayerPoint(
                id = "point-historic-apostles-creed",
                entityId = "historic-apostles-creed",
                title = "The Apostles' Creed",
                description = "I believe in God the Father Almighty, Maker of heaven and earth: And in Jesus Christ his only Son our Lord, Who was conceived by the Holy Ghost, Born of the Virgin Mary, Suffered under Pontius Pilate, Was crucified, dead, and buried: He descended into hell; The third day he rose again from the dead; He ascended into heaven, And sitteth on the right hand of God the Father Almighty; From thence he shall come to judge the quick and the dead. I believe in the Holy Ghost; The holy Catholick Church; The Communion of Saints; The Forgiveness of sins; The Resurrection of the body, And the life everlasting. Amen.",
                status = PrayerStatus.HISTORIC,
                createdAt = 1700000006000L
            )
        ),
        // 7. C.H. Spurgeon: Help from on High
        PreloadedHistoricTopic(
            entity = IndividualEntity(
                id = "historic-spurgeon-help-from-on-high",
                rootCode = RootCode.GENERAL,
                displayName = "C.H. Spurgeon: Help from on High",
                contextDescription = "Pulpit prayer at the Metropolitan Tabernacle (Passmore & Alabaster, 1905, Prayer I)",
                isPreloadedHistoric = true,
                createdAt = 1700000007000L
            ),
            prayerPoint = PrayerPoint(
                id = "point-historic-spurgeon-help-from-on-high",
                entityId = "historic-spurgeon-help-from-on-high",
                title = "Help from on High",
                description = "O Thou who art King of kings and Lord of lords, we worship Thee. We can truly say that we delight in God. Our longing is to feel Thy presence, and it is the heaven of heavens that Thou art there. Come near, our Father, come very near to Thy children. Some of us are very weak in body and faint in heart. Lay Thy right hand upon us and say unto us, 'Fear not.' Lord Jesus, take from us now everything that would hinder the closest communion with God. Our very highest prayer is for perfect holiness, complete consecration, entire cleansing from every evil. Take our heart, our head, our hands, our feet, and use us all for Thee. We ask in the name of Jesus Christ Thy Son. Amen.",
                status = PrayerStatus.HISTORIC,
                createdAt = 1700000007000L
            )
        ),
        // 8. C.H. Spurgeon: A Prayer for Holiness
        PreloadedHistoricTopic(
            entity = IndividualEntity(
                id = "historic-spurgeon-prayer-for-holiness",
                rootCode = RootCode.GENERAL,
                displayName = "C.H. Spurgeon: A Prayer for Holiness",
                contextDescription = "Pulpit prayer at the Metropolitan Tabernacle (Passmore & Alabaster, 1905, Prayer VIII)",
                isPreloadedHistoric = true,
                createdAt = 1700000008000L
            ),
            prayerPoint = PrayerPoint(
                id = "point-historic-spurgeon-prayer-for-holiness",
                entityId = "historic-spurgeon-prayer-for-holiness",
                title = "A Prayer for Holiness",
                description = "Our Father, we worship and love Thee; and it is one point of our worship that Thou art holy. Make us in love with goodness, purity, justice, and true holiness. O God, let us not live in vain. Cleanse us from secret faults, keep back Thy servants also from presumptuous sins; let them not have dominion over us. Work in us to will and to do of Thy good pleasure. May the life of Christ be copied in our lives, that men may take knowledge of us that we have been with Jesus, and have learned of Him. We ask all through Jesus Christ our Lord. Amen.",
                status = PrayerStatus.HISTORIC,
                createdAt = 1700000008000L
            )
        ),
        // 9. C.H. Spurgeon: Thanks Be Unto God
        PreloadedHistoricTopic(
            entity = IndividualEntity(
                id = "historic-spurgeon-thanks-be-unto-god",
                rootCode = RootCode.GENERAL,
                displayName = "C.H. Spurgeon: Thanks Be Unto God",
                contextDescription = "Pulpit prayer at the Metropolitan Tabernacle (Passmore & Alabaster, 1905, Prayer II)",
                isPreloadedHistoric = true,
                createdAt = 1700000009000L
            ),
            prayerPoint = PrayerPoint(
                id = "point-historic-spurgeon-thanks-be-unto-god",
                entityId = "historic-spurgeon-thanks-be-unto-god",
                title = "Thanks Be Unto God",
                description = "O Lord God, help us now really to worship Thee. Wilt Thou shut the door upon the world for us? Help us to forget our cares. Enable us to rise clean out of this world. May we get rid of all its down-dragging tendencies. O Thou precious Lord Jesus Christ, we do adore Thee with all our hearts. Thou hast paid Thy life for Thy people; Thou hast ransomed Thy folk with Thy heart's blood. Be Thou, therefore, for ever beloved and adored. Unite our hearts with Thine own, and be Thou heart and soul and life and everything to us. Reign, Immanuel, reign; sit on the high throne, and let Christ alone reign. Amen.",
                status = PrayerStatus.HISTORIC,
                createdAt = 1700000009000L
            )
        ),
        // 10. C.H. Spurgeon: The Love Without Measure
        PreloadedHistoricTopic(
            entity = IndividualEntity(
                id = "historic-spurgeon-love-without-measure",
                rootCode = RootCode.GENERAL,
                displayName = "C.H. Spurgeon: Love Without Measure",
                contextDescription = "Pulpit prayer at the Metropolitan Tabernacle (Passmore & Alabaster, 1905, Prayer III)",
                isPreloadedHistoric = true,
                createdAt = 1700000010000L
            ),
            prayerPoint = PrayerPoint(
                id = "point-historic-spurgeon-love-without-measure",
                entityId = "historic-spurgeon-love-without-measure",
                title = "The Love Without Measure",
                description = "Lord, we would come to Thee, but do Thou come to us. Draw us and we will run after Thee. Blessed Spirit, help our infirmities, for we know not what we should pray for as we ought. We thank Thee, Lord, for the love without beginning which chose us or ever the earth was; for the love without measure which entered into covenant for our redemption; for the love without failure which in due time appeared in the person of Christ and wrought out our redemption; for that love which has never changed, though we have wandered. Perfect Thy work within our hearts. Grant that we may abide in Christ and live near to God, through Jesus Christ our Lord. Amen.",
                status = PrayerStatus.HISTORIC,
                createdAt = 1700000010000L
            )
        ),
        // 11. C.H. Spurgeon: The All-Prevailing Plea
        PreloadedHistoricTopic(
            entity = IndividualEntity(
                id = "historic-spurgeon-all-prevailing-plea",
                rootCode = RootCode.GENERAL,
                displayName = "C.H. Spurgeon: The All-Prevailing Plea",
                contextDescription = "Pulpit prayer at the Metropolitan Tabernacle (Passmore & Alabaster, 1905, Prayer IV)",
                isPreloadedHistoric = true,
                createdAt = 1700000011000L
            ),
            prayerPoint = PrayerPoint(
                id = "point-historic-spurgeon-all-prevailing-plea",
                entityId = "historic-spurgeon-all-prevailing-plea",
                title = "The All-Prevailing Plea",
                description = "O Lord God, the Fountain of all Fulness, we, who are nothing but emptiness, come unto Thee for all supplies, nor shall we come in vain, since we bear with us a plea which is all prevalent. We come commanded by Thy Word, encouraged by Thy promise, and preceded by Christ Jesus, our great High Priest. Unto Thee be glory, and honour, and power, and majesty, and dominion, and might, for ever and ever! Refresh every corner of the vineyard, and on every branch of the vine let the dew of heaven rest. Bless Thy church throughout the world, and come quickly, Lord Jesus. Amen.",
                status = PrayerStatus.HISTORIC,
                createdAt = 1700000011000L
            )
        ),
        // 12. C.H. Spurgeon: Under the Blood
        PreloadedHistoricTopic(
            entity = IndividualEntity(
                id = "historic-spurgeon-under-the-blood",
                rootCode = RootCode.GENERAL,
                displayName = "C.H. Spurgeon: Under the Blood",
                contextDescription = "Pulpit prayer at the Metropolitan Tabernacle (Passmore & Alabaster, 1905, Prayer XI)",
                isPreloadedHistoric = true,
                createdAt = 1700000012000L
            ),
            prayerPoint = PrayerPoint(
                id = "point-historic-spurgeon-under-the-blood",
                entityId = "historic-spurgeon-under-the-blood",
                title = "Under the Blood",
                description = "Jehovah our God, we thank Thee for leaving on record the story of Thy covenant faithfulness. Thou keepest Thy promises and Thy Word never faileth. We bless Thee that our sins are blotted out by the atoning sacrifice of Jesus. For all our wanderings, for all our shortcomings, for all our unfaithfulness, forgive us, O Lord, and wash us anew in the precious blood of the Lamb. Grant us grace to walk worthy of our high calling in Christ Jesus, keeping our garments unspotted from the world. In the name of our Saviour, Jesus Christ. Amen.",
                status = PrayerStatus.HISTORIC,
                createdAt = 1700000012000L
            )
        ),
        // 13. C.H. Spurgeon: The Peace of God
        PreloadedHistoricTopic(
            entity = IndividualEntity(
                id = "historic-spurgeon-peace-of-god",
                rootCode = RootCode.GENERAL,
                displayName = "C.H. Spurgeon: The Peace of God",
                contextDescription = "Pulpit prayer at the Metropolitan Tabernacle (Passmore & Alabaster, 1905, Prayer XV)",
                isPreloadedHistoric = true,
                createdAt = 1700000013000L
            ),
            prayerPoint = PrayerPoint(
                id = "point-historic-spurgeon-peace-of-god",
                entityId = "historic-spurgeon-peace-of-god",
                title = "The Peace of God",
                description = "Our God, we stand not afar off as Israel did at Sinai, nor does a veil hang dark between Thy face and ours; but the veil is rent by the death of our Divine Lord and Mediator, Jesus Christ, and in His name we come up to the mercy seat, and here we present our prayers and our praises accepted in Him. Grant us that peace of God which passeth all understanding. Quiet our hearts in Thine own love; let no worldly care disturb the deep calm of our spirits in Christ Jesus. Unto Him who loved us and washed us from our sins in His own blood, be glory for ever. Amen.",
                status = PrayerStatus.HISTORIC,
                createdAt = 1700000013000L
            )
        ),
        // 14. C.H. Spurgeon: The Great Sacrifice
        PreloadedHistoricTopic(
            entity = IndividualEntity(
                id = "historic-spurgeon-the-great-sacrifice",
                rootCode = RootCode.GENERAL,
                displayName = "C.H. Spurgeon: The Great Sacrifice",
                contextDescription = "Pulpit prayer at the Metropolitan Tabernacle (Passmore & Alabaster, 1905, Prayer XX)",
                isPreloadedHistoric = true,
                createdAt = 1700000014000L
            ),
            prayerPoint = PrayerPoint(
                id = "point-historic-spurgeon-the-great-sacrifice",
                entityId = "historic-spurgeon-the-great-sacrifice",
                title = "The Great Sacrifice",
                description = "O God our Father, blessed be Thy name for ever, Thou didst deliver us. We come again now to the cross whereon the Saviour bled; we give another look of faith to Him. Lord God, we see in Thy crucified Son a sacrifice for sin; we see how Thou hast made Him to be sin for us that we might be made the righteousness of God in Him. We are not our own; we are bought with a price. Lord Jesus, renew Thy grasp of us; take us over again, for we surrender ourselves to Thee. Set in us anew the marks and tokens of Thy possession, that we may live for Thee henceforth and for ever. Amen.",
                status = PrayerStatus.HISTORIC,
                createdAt = 1700000014000L
            )
        )
    )

    val HISTORIC_ENTITY_ID: String = HISTORIC_TOPICS.first().entity.id

    fun getHistoricEntities(): List<IndividualEntity> = HISTORIC_TOPICS.map { it.entity }

    fun getHistoricPrayerPoints(): List<PrayerPoint> = HISTORIC_TOPICS.map { it.prayerPoint }

    // Backward-compatible single entity lookup
    fun getHistoricEntity(): IndividualEntity = HISTORIC_TOPICS.first().entity
}
