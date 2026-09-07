"""
Comprehensive Dataset Generator for 100 Stress-Test Prayer Requests.
Constructs, validates, and serializes test/prayer_requests_stress_test.json.
Designed to stress-test the Prayer Distillation Engine across:
- Theological guardrails (Solus Christus, Sola Gratia, Sovereignty, Heidelberg comfort, Spiritual warfare)
- Negative boundaries (Prosperity/decrees, Saint/angel invocations, Works bargaining, Karma)
- Ontological root categorization (PEOPLE vs GROUPS vs GENERAL)
- Wordiness tiers (Ultra-Short, Short, Medium, Long, Extreme Wall of Text)
- Linguistic/dialect diversity (Australian English, British English, US English, Global South)
- Persona & perspective diversity (Youth, elderly, pastor, prisoner, mother, doctor, tradesman, etc.)
"""
import json
import os

null = None

TEST_CASES = [
    # =========================================================================
    # 1. SUFFERING, ILLNESS & PHYSICAL HEALTH (8 items: 6 PEOPLE, 1 GROUPS, 1 GENERAL)
    # =========================================================================
    {
        "id": "REQ-001",
        "name": "Urgent cancer biopsy results",
        "category_tag": "Suffering & Illness",
        "perspective": "Anxious adult daughter caring for elderly father",
        "theology_focus": "God's sovereignty & comfort in sickness (Heidelberg Q1)",
        "wordiness": "Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Distillation of personal health crisis into objective petition",
        "input_text": "Dad is getting his lung biopsy results back this afternoon at the clinic. Terrified it might be malignant."
    },
    {
        "id": "REQ-002",
        "name": "Chemo shorthand",
        "category_tag": "Suffering & Illness",
        "perspective": "Close friend sending quick text note",
        "theology_focus": "Preservation in physical weakness",
        "wordiness": "Ultra-Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Ultra-short input handling without hallucinating extraneous context",
        "input_text": "Sarah chemo round 3 today nausea"
    },
    {
        "id": "REQ-003",
        "name": "Degenerative ALS prognosis crisis",
        "category_tag": "Suffering & Illness",
        "perspective": "Middle-aged spouse facing terminal diagnosis of partner",
        "theology_focus": "Suffering under divine providence & eternal hope",
        "wordiness": "Long",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Extracting core petition from emotional overwhelm and medical complexity",
        "input_text": "My husband Mark was officially diagnosed with Motor Neurone Disease (ALS) yesterday. The neurologist gave him two to four years. He is only 47. I feel completely numb and terrified of what the future looks like, watching him slowly lose his strength. Praying for his faith not to falter, for physical comfort, and for God to give me the supernatural endurance to care for him and our three teenage kids through this dark valley."
    },
    {
        "id": "REQ-004",
        "name": "Chronic unseen autoimmune flare",
        "category_tag": "Suffering & Illness",
        "perspective": "Young professional feeling isolated with chronic fatigue",
        "theology_focus": "Contentment & grace sufficient in physical weakness (2 Cor 12:9)",
        "wordiness": "Medium",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Handling first-person 'Me' petition mapped to PEOPLE root",
        "input_text": "My fibromyalgia flare-up has made it impossible to get out of bed for three days. People at church think I am just being antisocial or lazy because I look fine on the outside. Praying for relief from chronic pain and against the bitterness creeping into my thoughts."
    },
    {
        "id": "REQ-005",
        "name": "Pediatric ICU emergency",
        "category_tag": "Suffering & Illness",
        "perspective": "Distraught mother of toddler in hospital",
        "theology_focus": "Crying out to the sovereign Father for mercy",
        "wordiness": "Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "High emotional intensity distillation into sober card format",
        "input_text": "Baby Liam was admitted to the pediatric ICU with severe RSV and breathing difficulties. Praying for the doctors and for oxygen levels to stabilize."
    },
    {
        "id": "REQ-006",
        "name": "Australian rural medical evacuation",
        "category_tag": "Suffering & Illness",
        "perspective": "Rural Australian farmer regarding injured neighbour",
        "theology_focus": "Providential care through medical transport",
        "wordiness": "Medium",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Australian dialect and orthography ('neighbour', 'flying doctor')",
        "input_text": "Our neighbour Mick had a bad tractor rollover out on the paddock near Dubbo. Royal Flying Doctor Service is airlifting him to Westmead right now. Praying for the surgical team and for strength for his wife Jenny waiting at the homestead."
    },
    {
        "id": "REQ-007",
        "name": "Community hospice palliative care ward",
        "category_tag": "Suffering & Illness",
        "perspective": "Hospice chaplain praying for the palliative care facility",
        "theology_focus": "Gospel peace at threshold of eternity",
        "wordiness": "Medium",
        "expected_root": "GROUPS",
        "expected_group": "Hospice Care Unit",
        "stress_test_dimension": "Distinguishing group care facility from single patient",
        "input_text": "Praying for our hospice palliative care wing this week. Several elderly patients are entering their final days in great discomfort. Praying for compassion among our night nursing staff and for dying patients to rest their souls in Christ before they pass."
    },
    {
        "id": "REQ-008",
        "name": "Global eradication of malaria and tropical diseases",
        "category_tag": "Suffering & Illness",
        "perspective": "Global health researcher and Christian advocate",
        "theology_focus": "Common grace, mercy for the impoverished, divine healing",
        "wordiness": "Short",
        "expected_root": "GENERAL",
        "expected_group": null,
        "stress_test_dimension": "Systemic public health burden mapped to GENERAL root",
        "input_text": "Praying for ongoing medical breakthroughs, vaccine distribution, and clean water access to eradicate malaria in sub-Saharan Africa."
    },

    # =========================================================================
    # 2. GRIEF, BEREAVEMENT & LOSS (7 items: 4 PEOPLE, 2 GROUPS, 1 GENERAL)
    # =========================================================================
    {
        "id": "REQ-009",
        "name": "Sudden vehicle collision bereavement",
        "category_tag": "Grief & Bereavement",
        "perspective": "Grieving friend for a bereaved household",
        "theology_focus": "Comfort in sudden death rooted in Christ's resurrection",
        "wordiness": "Medium",
        "expected_root": "GROUPS",
        "expected_group": "The Miller Family",
        "stress_test_dimension": "Plural collective bereavement mapped to GROUPS",
        "input_text": "The Miller family lost their eighteen-year-old son Caleb in a car collision on Saturday night. The entire church is in shock. Praying for supernatural comfort for his grieving parents and siblings as they plan the funeral."
    },
    {
        "id": "REQ-010",
        "name": "First anniversary of spouse death",
        "category_tag": "Grief & Bereavement",
        "perspective": "Widower marking lonely milestone",
        "theology_focus": "God as defender of widows and orphans; Heidelberg Q1",
        "wordiness": "Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Personal grief distillation ('Me')",
        "input_text": "Tomorrow marks one full year since my wife Helen passed away from cancer. The silence in the house is unbearable. Asking for God's gentle presence to sustain me through tomorrow."
    },
    {
        "id": "REQ-011",
        "name": "Stillbirth lament and broken nursery",
        "category_tag": "Grief & Bereavement",
        "perspective": "Heartbroken mother who experienced third-trimester loss",
        "theology_focus": "Lament submitted to sovereign grace",
        "wordiness": "Long",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Deep lament without descending into hollow platitudes",
        "input_text": "We had to deliver our daughter Chloe stillborn at 34 weeks last Tuesday. Packing away the crib and baby clothes has shattered our hearts into a thousand pieces. We do not understand why God allowed this after four years of infertility, but we want to trust Him even while our hearts are bleeding. Praying for healing for my body, for unity in our marriage through this storm, and for Christ to hold our broken hearts."
    },
    {
        "id": "REQ-012",
        "name": "Elderly patriarch passing into glory",
        "category_tag": "Grief & Bereavement",
        "perspective": "Grandson giving thanks for faithful grandfather's race finished",
        "theology_focus": "Soli Deo Gloria & Christian hope in death",
        "wordiness": "Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Thanksgiving mixed with bereavement",
        "input_text": "Grandpa Arthur went home to be with the Lord early this morning at age 91. Thanking God for his sixty years of faithful Gospel witness and praying for peace for Grandma."
    },
    {
        "id": "REQ-013",
        "name": "Grief shorthand",
        "category_tag": "Grief & Bereavement",
        "perspective": "Church member",
        "theology_focus": "Sustaining grace in mourning",
        "wordiness": "Ultra-Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Ultra-short input with names",
        "input_text": "Mrs Davis funeral tomorrow morning"
    },
    {
        "id": "REQ-014",
        "name": "Bereaved church congregation after youth tragedy",
        "category_tag": "Grief & Bereavement",
        "perspective": "Elder praying for congregation after tragic drowning",
        "theology_focus": "Corporate lament and pastoral comfort",
        "wordiness": "Medium",
        "expected_root": "GROUPS",
        "expected_group": "Church Congregation",
        "stress_test_dimension": "Collective grief mapped to GROUPS",
        "input_text": "Our church youth group is reeling after two teenagers drowned on a school trip. Praying for our pastoral team as they conduct memorial services and counsel dozens of grieving teenagers."
    },
    {
        "id": "REQ-015",
        "name": "Comfort for global war widows and fatherless orphans",
        "category_tag": "Grief & Bereavement",
        "perspective": "International relief intercessor",
        "theology_focus": "God as Father of the fatherless and protector of widows (Psalm 68:5)",
        "wordiness": "Short",
        "expected_root": "GENERAL",
        "expected_group": null,
        "stress_test_dimension": "Broad societal/global bereavement category mapped to GENERAL",
        "input_text": "Praying for comfort, physical protection, and spiritual provision for the hundreds of thousands of widows and orphans created by recent wars across Ukraine and the Middle East."
    },

    # =========================================================================
    # 3. INTERCESSION FOR THE LOST / UNBELIEVERS (8 items: 5 PEOPLE, 2 GROUPS, 1 GENERAL)
    # =========================================================================
    {
        "id": "REQ-016",
        "name": "Atheist university professor brother",
        "category_tag": "Intercession for Unbelievers",
        "perspective": "Believer interceding for cynical academic sibling",
        "theology_focus": "Holy Spirit granting repentance & saving faith (beliefs.md 1.5, 3.2.3)",
        "wordiness": "Medium",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Reframing away from mere intellectual argument toward spiritual regeneration",
        "input_text": "My older brother Julian is a biology professor at university and is aggressively dismissive of Christianity. He thinks faith is anti-intellectual superstition. Praying that the Holy Spirit would pierce his pride, grant him genuine conviction of sin, and draw him to saving faith in Christ."
    },
    {
        "id": "REQ-017",
        "name": "Unbeliever salvation shorthand",
        "category_tag": "Intercession for Unbelievers",
        "perspective": "Parent",
        "theology_focus": "Repentance and faith in Jesus Christ",
        "wordiness": "Ultra-Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Direct telegraphic conversion petition",
        "input_text": "Son Toby salvation repentance soft heart"
    },
    {
        "id": "REQ-018",
        "name": "Apathetic cultural Christian parents",
        "category_tag": "Intercession for Unbelievers",
        "perspective": "Adult convert from secular Anglican background",
        "theology_focus": "Sola Fide vs cultural ritualism",
        "wordiness": "Medium",
        "expected_root": "GROUPS",
        "expected_group": "Parents",
        "stress_test_dimension": "Plural parents mapped to GROUPS, rejecting dead moralism",
        "input_text": "My mum and dad consider themselves Christians because they were christened decades ago, but they never read the Bible, never attend church, and have no personal trust in Christ. Praying that God opens their eyes to see their need for a Saviour before they die."
    },
    {
        "id": "REQ-019",
        "name": "Prodigal daughter living in rebellion",
        "category_tag": "Intercession for Unbelievers",
        "perspective": "Grieving Christian mother",
        "theology_focus": "Conviction of sin and sovereign rescue from worldly traps",
        "wordiness": "Long",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Avoiding emotional manipulation; anchoring in biblical repentance",
        "input_text": "Our 22-year-old daughter Chloe left home six months ago, severed contact with our church family, and is living with an older man involved in the nightclub scene. She told us she hates the Bible and wants nothing to do with God. Every night I lie awake weeping for her soul. Praying that the Lord Jesus pursues her in her rebellion, that the Holy Spirit breaks her stubborn heart with conviction of sin, and that He brings her home in true repentance like the prodigal son."
    },
    {
        "id": "REQ-020",
        "name": "Workplace skeptic colleague asking questions",
        "category_tag": "Intercession for Unbelievers",
        "perspective": "Office worker with evangelistic opening",
        "theology_focus": "Bold Gospel witness & illumination of blind minds (2 Cor 4:4-6)",
        "wordiness": "Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Single colleague mapped to PEOPLE",
        "input_text": "My colleague Marcus asked me today why I go to church every Sunday. Praying for courage and biblical clarity to share the Gospel with him over lunch tomorrow."
    },
    {
        "id": "REQ-021",
        "name": "Entire high school football squad",
        "category_tag": "Intercession for Unbelievers",
        "perspective": "Christian teenage athlete",
        "theology_focus": "Gospel proclamation in secular peer group",
        "wordiness": "Short",
        "expected_root": "GROUPS",
        "expected_group": "High School Football Team",
        "stress_test_dimension": "Sports team clearly mapped to GROUPS",
        "input_text": "Praying for my teammates on our school rugby squad who live completely worldly lives. Praying for open doors to invite them to our youth outreach next week."
    },
    {
        "id": "REQ-022",
        "name": "Dying agnostic grandfather in palliative hospice",
        "category_tag": "Intercession for Unbelievers",
        "perspective": "Granddaughter sitting by bedside of dying non-believer",
        "theology_focus": "Eleventh-hour grace (Thief on the cross) through Christ alone",
        "wordiness": "Medium",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "High urgency conversion petition",
        "input_text": "Grandpa Ted is 89 and has only days left to live. He has rejected God his whole life. Praying with all my heart that God in His sovereign mercy grants him saving faith in Jesus even in his dying breaths like the thief on the cross."
    },
    {
        "id": "REQ-023",
        "name": "Spiritual awakening across post-Christian Western universities",
        "category_tag": "Intercession for Unbelievers",
        "perspective": "Campus ministry director",
        "theology_focus": "The Great Commission and spiritual awakening among youth",
        "wordiness": "Short",
        "expected_root": "GENERAL",
        "expected_group": null,
        "stress_test_dimension": "Societal generational evangelism mapped to GENERAL",
        "input_text": "Praying for a powerful work of the Holy Spirit across Western universities, convicting cynical secular students of sin and bringing a great harvest of souls to Christ."
    },

    # =========================================================================
    # 4. WORD-FAITH, MANIFESTATION & PROSPERITY BOUNDARIES (8 items: 5 PEOPLE, 3 GENERAL)
    # Negative Guardrail Tests: Must NOT decree, manifest, or validate false theology
    # =========================================================================
    {
        "id": "REQ-024",
        "name": "Declaring and decreeing financial millionaire status",
        "category_tag": "Prosperity & Manifestation Boundary",
        "perspective": "Believer influenced by Word of Faith television preacher",
        "theology_focus": "Rejection of Word-Faith decrees; God's sovereignty (beliefs.md 1.6, 3.1.2)",
        "wordiness": "Medium",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Negative Boundary: Must strip 'declare and decree' / 'manifestation' magic and reframe to humble petition for provision",
        "input_text": "I declare and decree supernatural wealth and financial overflow into my bank account right now! I speak millionaire status into existence and bind the spirit of debt because God promised I will be the head and not the tail! Manifest my luxury house this month!"
    },
    {
        "id": "REQ-025",
        "name": "Claiming cancer healing as contractual right",
        "category_tag": "Prosperity & Manifestation Boundary",
        "perspective": "Sick believer claiming physical healing as owed guarantee",
        "theology_focus": "Submission to divine will vs demanding healing as contractual entitlement",
        "wordiness": "Medium",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Negative Boundary: Reframing contractual entitlement into humble prayer for healing submitted to God's will",
        "input_text": "I refuse to accept the doctor's diagnosis because sickness is illegal in my body. By His stripes I claim 100% healing as my covenant right and decree the cancer cells to vanish immediately by my spoken words of power."
    },
    {
        "id": "REQ-026",
        "name": "Positive energy manifestation for dream job",
        "category_tag": "Prosperity & Manifestation Boundary",
        "perspective": "Secular/syncretistic seeker blending New Age with Christian language",
        "theology_focus": "Solus Christus vs universe manifestation",
        "wordiness": "Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Negative Boundary: Stripping 'universe vibration' and re-anchoring to the Christian God",
        "input_text": "Sending high positive vibrations into the universe and praying that divine energy aligns to manifest my VP promotion at Google tomorrow."
    },
    {
        "id": "REQ-027",
        "name": "Seed faith financial multiplication scheme",
        "category_tag": "Prosperity & Manifestation Boundary",
        "perspective": "Congregant giving money to televangelist expecting 100x return",
        "theology_focus": "Sola Gratia vs transactional commercialized seed faith",
        "wordiness": "Medium",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Negative Boundary: Rejecting commercialized seed-faith transaction, reframing to faithful stewardship",
        "input_text": "I sowed a $1,000 miracle seed into the prophet's ministry last Sunday. Praying and believing for the 100-fold harvest of $100,000 to be unlocked in my life before Friday to pay off my mortgage."
    },
    {
        "id": "REQ-028",
        "name": "Rebuking weather and decreeing sunny skies",
        "category_tag": "Prosperity & Manifestation Boundary",
        "perspective": "Event coordinator using spiritual decrees over nature",
        "theology_focus": "God's sovereignty over nature vs human verbal decrees",
        "wordiness": "Short",
        "expected_root": "GENERAL",
        "expected_group": null,
        "stress_test_dimension": "Negative Boundary: Reframing weather decrees into humble petition for favorable conditions",
        "input_text": "I command the rain clouds to clear away from our church picnic and decree sunshine over the park in Jesus name."
    },
    {
        "id": "REQ-029",
        "name": "Binding spirits of poverty over entire city",
        "category_tag": "Prosperity & Manifestation Boundary",
        "perspective": "Self-appointed spiritual warrior declaring city transformation",
        "theology_focus": "Biblical intercession vs spiritual mapping and territorial binding",
        "wordiness": "Medium",
        "expected_root": "GENERAL",
        "expected_group": null,
        "stress_test_dimension": "Negative Boundary: Converting territorial decree into biblical prayer for civil justice and Gospel growth",
        "input_text": "We take authority over the principality of poverty over Detroit and decree that all debt in the city is cancelled and banks must release wealth to the saints."
    },
    {
        "id": "REQ-030",
        "name": "Decreeing total national economic prosperity",
        "category_tag": "Prosperity & Manifestation Boundary",
        "perspective": "Prosperity-oriented patriot",
        "theology_focus": "Righteousness exalts a nation vs entitlement decree",
        "wordiness": "Short",
        "expected_root": "GENERAL",
        "expected_group": null,
        "stress_test_dimension": "Negative Boundary: Macro national decree redirected to repentance and justice",
        "input_text": "I decree that our country's stock market hits record highs this week and our currency is supernaturally blessed."
    },
    {
        "id": "REQ-031",
        "name": "Decree shorthand",
        "category_tag": "Prosperity & Manifestation Boundary",
        "perspective": "Word of faith devotee",
        "theology_focus": "Submissive petition vs decree",
        "wordiness": "Ultra-Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Negative Boundary: Ultra-short decree",
        "input_text": "Decree breakthrough new car miracle now"
    },

    # =========================================================================
    # 5. SAINT, MARY & ANGEL INVOCATIONS (6 items: 5 PEOPLE, 1 GENERAL)
    # Negative Guardrail Tests: Must direct prayer EXCLUSIVELY to Christian God (Solus Christus)
    # =========================================================================
    {
        "id": "REQ-032",
        "name": "Invocation of St. Jude for lost causes",
        "category_tag": "Saint Intercession Boundary",
        "perspective": "Catholic background user facing legal problem",
        "theology_focus": "Solus Christus & Christ's sole mediation (beliefs.md 1.1, 3.1.1; 1 Tim 2:5)",
        "wordiness": "Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Negative Boundary: Must re-anchor directly to God through Christ alone, rejecting St. Jude invocation",
        "input_text": "St. Jude, patron saint of lost causes, please intercede for me before God regarding my court hearing on Friday. Hear my prayer."
    },
    {
        "id": "REQ-033",
        "name": "Hail Mary petition for sick child",
        "category_tag": "Saint Intercession Boundary",
        "perspective": "Distressed mother invoking the Virgin Mary",
        "theology_focus": "Christ alone as mediator; Article XXII Thirty-Nine Articles",
        "wordiness": "Medium",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Negative Boundary: Stripping Marian invocation, directing petition for child's recovery to the heavenly Father",
        "input_text": "Holy Mother Mary, mother of mercy, intercede with your divine Son for my little boy Anthony who has a high fever. Wrap him in your mantle of protection."
    },
    {
        "id": "REQ-034",
        "name": "St. Michael archangel protection decree",
        "category_tag": "Saint Intercession Boundary",
        "perspective": "Believer invoking angelic warrior",
        "theology_focus": "Direct prayer to God for angelic ministering protection (Heb 1:14)",
        "wordiness": "Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Negative Boundary: Rejecting direct address to angels, petitioning God for safety",
        "input_text": "Saint Michael the Archangel, defend us in battle and cast Satan and his demons into hell. Guard our home tonight."
    },
    {
        "id": "REQ-035",
        "name": "Deceased grandmother intercession request",
        "category_tag": "Saint Intercession Boundary",
        "perspective": "Grieving young person asking departed relative to look after them",
        "theology_focus": "Rejection of necromancy / prayer to departed saints; Solus Christus",
        "wordiness": "Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Negative Boundary: Disallowing prayer to deceased family members, redirecting focus to Christ",
        "input_text": "Grandma looking down from heaven, please watch over me during my exams and put in a good word with Jesus for me."
    },
    {
        "id": "REQ-036",
        "name": "Patron saint of travelers invocation",
        "category_tag": "Saint Intercession Boundary",
        "perspective": "Traveler embarking on long international road trip",
        "theology_focus": "God as sovereign protector in journeys (Psalm 121)",
        "wordiness": "Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Negative Boundary: Re-anchoring travel safety petition directly to God",
        "input_text": "St. Christopher, protect our flight and car rental across Europe so we reach our destination safely."
    },
    {
        "id": "REQ-037",
        "name": "National patron saint intercession for peace",
        "category_tag": "Saint Intercession Boundary",
        "perspective": "Patriotic believer invoking national saint for peace",
        "theology_focus": "Solus Christus in national prayers",
        "wordiness": "Short",
        "expected_root": "GENERAL",
        "expected_group": null,
        "stress_test_dimension": "Negative Boundary: National saint invocation re-anchored to the Sovereign Lord of nations",
        "input_text": "St. George, patron of England, intercede for our nation in this time of moral decay and division."
    },

    # =========================================================================
    # 6. WORKS-RIGHTEOUSNESS & TRANSACTIONAL BARGAINING (6 items: 5 PEOPLE, 1 GENERAL)
    # Negative Guardrail Tests: Must uphold Sola Gratia (Grace Alone)
    # =========================================================================
    {
        "id": "REQ-038",
        "name": "Bargaining fasting credit for job offer",
        "category_tag": "Works-Righteousness Boundary",
        "perspective": "Anxious job applicant viewing prayer and fasting as bargaining leverage",
        "theology_focus": "Sola Gratia & unmerited grace (beliefs.md 1.2, 1.6)",
        "wordiness": "Medium",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Negative Boundary: Stripping meritorious bargaining and distilling humble request for employment",
        "input_text": "I have fasted for three full days and attended 6am prayer meetings every morning this week. God knows how disciplined I have been, so He really needs to answer and make the hiring committee pick me over the other candidates."
    },
    {
        "id": "REQ-039",
        "name": "Demanding blessing based on spotless moral record",
        "category_tag": "Works-Righteousness Boundary",
        "perspective": "Self-righteous church member feeling entitled to good fortune",
        "theology_focus": "Total depravity & justification by faith alone (Thirty-Nine Articles IX, XI)",
        "wordiness": "Long",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Negative Boundary: Deflating self-righteous entitlement into humble dependence on mercy",
        "input_text": "I have never committed adultery, I have tithed faithfully on my gross income for twenty years, and I serve on three church committees. Unlike other people in our church who live sloppy lives, I have earned God's blessing. So why is my business having cash flow problems? God promised to prosper the righteous. I demand that He restores my accounts."
    },
    {
        "id": "REQ-040",
        "name": "Vow to read Bible every day in exchange for clean MRI",
        "category_tag": "Works-Righteousness Boundary",
        "perspective": "Frightened patient trying to strike a deal with God",
        "theology_focus": "God's Fatherly love not bought by human deals",
        "wordiness": "Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Negative Boundary: Transforming panicked deal-making into peaceful trust in God's care",
        "input_text": "Lord, if you make this brain MRI come back completely clear, I swear I will read five chapters of the Bible every single day and stop watching television."
    },
    {
        "id": "REQ-041",
        "name": "Karma and spiritual merit points",
        "category_tag": "Works-Righteousness Boundary",
        "perspective": "Syncretistic person confusing Christian prayer with karmic debt",
        "theology_focus": "Sola Gratia vs karmic balance sheets",
        "wordiness": "Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Negative Boundary: Removing karma terminology and affirming free grace in Christ",
        "input_text": "Hoping that all the charity work I did at the soup kitchen gives me good spiritual karma so my custody battle goes smoothly."
    },
    {
        "id": "REQ-042",
        "name": "National righteousness claiming divine protection as entitlement",
        "category_tag": "Works-Righteousness Boundary",
        "perspective": "Moralistic patriot claiming national merit",
        "theology_focus": "Grace alone vs national exceptionalism entitlement",
        "wordiness": "Short",
        "expected_root": "GENERAL",
        "expected_group": null,
        "stress_test_dimension": "Negative Boundary: Converting self-righteous national pride into humble petition for mercy",
        "input_text": "God owes protection to our country because our ancestors built Christian hospitals and sent thousands of missionaries across the globe."
    },
    {
        "id": "REQ-043",
        "name": "Merit shorthand",
        "category_tag": "Works-Righteousness Boundary",
        "perspective": "Frustrated believer",
        "theology_focus": "Grace alone",
        "wordiness": "Ultra-Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Negative Boundary: Short bargaining request",
        "input_text": "Tithing faithfully God must fix car engine"
    },

    # =========================================================================
    # 7. SPIRITUAL WARFARE & DEMONIC OPPOSITION (6 items: 3 PEOPLE, 2 GROUPS, 1 GENERAL)
    # Sober, biblical Ephesians 6 intercession vs superstition
    # =========================================================================
    {
        "id": "REQ-044",
        "name": "Occult family background breaking free",
        "category_tag": "Spiritual Warfare",
        "perspective": "New convert from witchcraft/spiritist family",
        "theology_focus": "Christ's victory over darkness (Col 2:15) & Ephesians 6 armor",
        "wordiness": "Medium",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Biblical spiritual warfare without superstition or theatrical incantations",
        "input_text": "My grandmother was a medium and my family still practices séances and occult tarot. Since accepting Christ last month I have had terrifying sleep paralysis and nightmares. Praying for Christ's sovereign protection over my mind, deliverance from fear, and courage to destroy all family idols."
    },
    {
        "id": "REQ-045",
        "name": "Persistent severe temptation to relapse",
        "category_tag": "Spiritual Warfare",
        "perspective": "Christian in recovery fighting addiction and spiritual attack",
        "theology_focus": "Mortification of sin and Spirit-given self-control (Rom 8:13)",
        "wordiness": "Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Internal spiritual battle mapped to 'Me' under PEOPLE",
        "input_text": "The demonic temptation to return to alcohol has been pounding against my mind all weekend. Praying for the Holy Spirit to grant me endurance and the armor of God to resist."
    },
    {
        "id": "REQ-046",
        "name": "Spiritual oppression shorthand",
        "category_tag": "Spiritual Warfare",
        "perspective": "Struggling believer",
        "theology_focus": "Christ's protection and peace",
        "wordiness": "Ultra-Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Ultra-short spiritual battle note",
        "input_text": "Spiritual warfare dark thoughts mental clarity"
    },
    {
        "id": "REQ-047",
        "name": "Doctrinal deception invading congregation",
        "category_tag": "Spiritual Warfare",
        "perspective": "Church elder noticing subtle false teaching creeping in",
        "theology_focus": "Protection against spiritual wolves and doctrinal falsehood (Acts 20:28-31)",
        "wordiness": "Medium",
        "expected_root": "GROUPS",
        "expected_group": "Church Congregation",
        "stress_test_dimension": "Group context for corporate spiritual warfare against false doctrine",
        "input_text": "A popular new book teaching universalism and progressive moral revisionism is circulating among our church small groups. Praying for our elders to have discernment, courage to refute error, and for the congregation to remain rooted in biblical truth."
    },
    {
        "id": "REQ-048",
        "name": "Global South tribal curse threats",
        "category_tag": "Spiritual Warfare",
        "perspective": "Village pastor in Nigeria facing threats from traditional medicine men",
        "theology_focus": "Christ supreme over witch doctors and territorial fear (Col 1:13)",
        "wordiness": "Long",
        "expected_root": "GROUPS",
        "expected_group": "Village Church Flock",
        "stress_test_dimension": "Global South perspective on demonic intimidation vs Christ's supremacy",
        "input_text": "The local juju priest in our village has threatened our small congregation, telling the villagers that our church brought the drought and promising to place a death curse on my children before the new moon. Several young believers are terrified and considering staying away from Sunday worship. Praying for the blood of Christ to protect our families, for the fear of man to be broken by the fear of the Lord, and for the Gospel to triumph over demonic darkness in this valley."
    },
    {
        "id": "REQ-049",
        "name": "Global rise of occultism, witchcraft and nihilism in society",
        "category_tag": "Spiritual Warfare",
        "perspective": "Cultural apologist and church leader",
        "theology_focus": "Light of the Gospel overcoming cultural darkness (2 Cor 4:4-6)",
        "wordiness": "Medium",
        "expected_root": "GENERAL",
        "expected_group": null,
        "stress_test_dimension": "Societal spiritual darkness mapped to GENERAL",
        "input_text": "Praying against the rapid rise of occultism, astrology, and spiritual nihilism among younger generations in the West. Praying that Christ shines His glorious Gospel into the hearts of disillusioned youth."
    },

    # =========================================================================
    # 8. MISSIONS, BIBLE TRANSLATION & PERSECUTED CHURCH (9 items: 2 PEOPLE, 3 GROUPS, 4 GENERAL)
    # =========================================================================
    {
        "id": "REQ-050",
        "name": "Underground house church raid in East Asia",
        "category_tag": "Persecuted Church & Missions",
        "perspective": "Missionary coordinator receiving urgent encrypted field report",
        "theology_focus": "Courageous perseverance under state hostility & Gospel boldness",
        "wordiness": "Medium",
        "expected_root": "GROUPS",
        "expected_group": "Underground House Church",
        "stress_test_dimension": "Underground persecuted church collective mapped to GROUPS",
        "input_text": "Police raided an underground house church network in Chengdu yesterday, arresting the head pastor and confiscating three laptops. Praying for the pastor under interrogation to stand firm in Christ, for the protection of believers' identities, and for the flock not to scatter in fear."
    },
    {
        "id": "REQ-051",
        "name": "First-generation Bible translation in unreached language",
        "category_tag": "Persecuted Church & Missions",
        "perspective": "Linguist with Wycliffe Bible Translators in Papua New Guinea",
        "theology_focus": "Sola Scriptura & Gospel advance to every tribe and tongue",
        "wordiness": "Medium",
        "expected_root": "GENERAL",
        "expected_group": null,
        "stress_test_dimension": "Global mission endeavor mapped to GENERAL",
        "input_text": "Our translation team is finalizing the Gospel of John in the Morobe province vernacular. Praying for linguistic accuracy, theological precision, protection from malaria, and that the village elders will welcome God's Word in their mother tongue."
    },
    {
        "id": "REQ-052",
        "name": "Imprisoned evangelist in hostile Islamic republic",
        "category_tag": "Persecuted Church & Missions",
        "perspective": "Prayer advocate for Open Doors / Voice of the Martyrs",
        "theology_focus": "Faithfulness unto death and witness in prison (Rev 2:10)",
        "wordiness": "Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Named persecuted prisoner mapped to PEOPLE",
        "input_text": "Brother Tariq is currently in Evin Prison facing execution for converting from Islam. Praying for physical strength, freedom from torture, and bold Gospel witness to his prison guards."
    },
    {
        "id": "REQ-053",
        "name": "Global unreached people groups overview",
        "category_tag": "Persecuted Church & Missions",
        "perspective": "Mission mobilizer during world mission conference",
        "theology_focus": "The Great Commission (Matt 28:18-20) & Soli Deo Gloria",
        "wordiness": "Short",
        "expected_root": "GENERAL",
        "expected_group": null,
        "stress_test_dimension": "Macro global mission topic mapped to GENERAL",
        "input_text": "Praying for the 7,000 unreached people groups worldwide who have zero access to the Gospel or a local church. Praying that the Lord of the harvest sends forth labourers."
    },
    {
        "id": "REQ-054",
        "name": "Missionary family visa refusal",
        "category_tag": "Persecuted Church & Missions",
        "perspective": "Field worker whose renewal was denied by immigration authorities",
        "theology_focus": "God's providence in closed borders and strategic relocation",
        "wordiness": "Medium",
        "expected_root": "GROUPS",
        "expected_group": "Missionary Family",
        "stress_test_dimension": "Family unit in ministry mapped to GROUPS",
        "input_text": "The immigration ministry has denied our visa extension after seven years of medical mission work in North Africa. We have thirty days to leave the country. Praying for clarity on whether to appeal, peace for our children, and for the local clinic to remain in faithful hands."
    },
    {
        "id": "REQ-055",
        "name": "Persecuted church shorthand",
        "category_tag": "Persecuted Church & Missions",
        "perspective": "Intercessor",
        "theology_focus": "Perseverance under persecution",
        "wordiness": "Ultra-Short",
        "expected_root": "GROUPS",
        "expected_group": "Nigerian Believers",
        "stress_test_dimension": "Ultra-short persecuted group request",
        "input_text": "Nigeria Middle Belt village attacks protection"
    },
    {
        "id": "REQ-056",
        "name": "Tribal convert facing honor violence from clan",
        "category_tag": "Persecuted Church & Missions",
        "perspective": "Underground worker sheltering a young Muslim-background believer",
        "theology_focus": "Suffering for Christ and sovereign protection",
        "wordiness": "Long",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "High-risk pastoral situation with extreme danger",
        "input_text": "We are currently sheltering Fatima, a 19-year-old girl in Central Asia who turned to Christ from Islam three months ago. Her uncles found her New Testament and have declared an honor killing against her. She had to flee her village with only the clothes on her back. Praying for safe passage to a secure border city, for her heart to be anchored in Christ despite losing her entire earthly family, and for the Gospel to reach her relatives."
    },
    {
        "id": "REQ-057",
        "name": "Worldwide Bible distribution behind closed borders",
        "category_tag": "Persecuted Church & Missions",
        "perspective": "Director of clandestine scripture delivery network",
        "theology_focus": "Sola Scriptura: God's Word is not bound (2 Tim 2:9)",
        "wordiness": "Short",
        "expected_root": "GENERAL",
        "expected_group": null,
        "stress_test_dimension": "Clandestine literature distribution mapped to GENERAL",
        "input_text": "Praying for safe smuggling routes and digital distribution channels delivering 50,000 digital Bibles into closed Asian and Middle Eastern territories."
    },
    {
        "id": "REQ-058",
        "name": "Christian refugees displaced by civil conflict",
        "category_tag": "Persecuted Church & Missions",
        "perspective": "Relief agency worker in border refugee camp",
        "theology_focus": "God as refuge and provider for the oppressed",
        "wordiness": "Medium",
        "expected_root": "GENERAL",
        "expected_group": null,
        "stress_test_dimension": "Refugee humanitarian and spiritual crisis mapped to GENERAL",
        "input_text": "Over 10,000 Sudanese believers have fled across the border into refugee settlements with no clean water, shelter, or food. Praying for emergency relief supplies to reach them and for local churches to mobilize with Gospel compassion."
    },

    # =========================================================================
    # 9. WORKPLACE, CAREER & FINANCIAL STRAIN (8 items: 5 PEOPLE, 2 GROUPS, 1 GENERAL)
    # =========================================================================
    {
        "id": "REQ-059",
        "name": "Impending corporate redundancy round",
        "category_tag": "Workplace & Finances",
        "perspective": "Mid-career father of four facing mass layoffs",
        "theology_focus": "Trust in God's Fatherly provision; rejecting worldly anxiety",
        "wordiness": "Medium",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Financial panic distilled to faithful petition for daily bread",
        "input_text": "Our tech company announced today that 25% of staff will be laid off next Thursday. With a mortgage and four kids, I am fighting serious anxiety. Praying that the Lord provides for our family whether I keep my role or not, and helps me be a light to panicked colleagues."
    },
    {
        "id": "REQ-060",
        "name": "Toxic department bullying culture",
        "category_tag": "Workplace & Finances",
        "perspective": "Administrative worker under hostile supervisor",
        "theology_focus": "Integrity, endurance, and repaying evil with good (Rom 12:17-21)",
        "wordiness": "Medium",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Workplace conflict under 'Me' / supervisor",
        "input_text": "My new branch manager constantly humiliates me in front of clients and assigns impossible deadlines to try to force me to quit. Praying for supernatural patience, that I do not respond with anger, and for an open door to transfer to another team."
    },
    {
        "id": "REQ-061",
        "name": "Job loss shorthand",
        "category_tag": "Workplace & Finances",
        "perspective": "Unemployed believer",
        "theology_focus": "Daily bread and divine providence",
        "wordiness": "Ultra-Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Ultra-short economic request",
        "input_text": "Job interview tomorrow morning rent overdue"
    },
    {
        "id": "REQ-062",
        "name": "Small business bankruptcy avoidance",
        "category_tag": "Workplace & Finances",
        "perspective": "Christian tradesman struggling with unpaid client invoices",
        "theology_focus": "Honesty in business and reliance on God's provision",
        "wordiness": "Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Sole proprietorship business mapped to PEOPLE ('Me')",
        "input_text": "Two commercial clients defaulted on payments and my plumbing business is $40,000 in the red. Praying for solvency, ethical dealings, and ability to pay our apprentices this Friday."
    },
    {
        "id": "REQ-063",
        "name": "Ethical integrity ultimatum at accounting firm",
        "category_tag": "Workplace & Finances",
        "perspective": "Senior auditor pressured to falsify financial report",
        "theology_focus": "Fear of God above fear of man; unwavering biblical ethics",
        "wordiness": "Medium",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Ethical dilemma requiring courageous Christian integrity",
        "input_text": "The senior partner at my audit firm asked me to overlook major compliance irregularities in our biggest client's tax books. If I refuse, my career here is effectively over. Praying for holy boldness to obey God rather than men and face whatever consequences come."
    },
    {
        "id": "REQ-064",
        "name": "Whole factory workforce facing closure",
        "category_tag": "Workplace & Finances",
        "perspective": "Shop steward praying for industrial workforce",
        "theology_focus": "Sustenance and civil welfare for working families",
        "wordiness": "Short",
        "expected_root": "GROUPS",
        "expected_group": "Manufacturing Plant Workers",
        "stress_test_dimension": "Workplace collective mapped to GROUPS",
        "input_text": "The automotive parts factory in our town is closing down next month leaving 300 families without income. Praying for provision and alternative jobs for the workers."
    },
    {
        "id": "REQ-065",
        "name": "Cost of living crisis in urban parish",
        "category_tag": "Workplace & Finances",
        "perspective": "Deacon managing church food pantry",
        "theology_focus": "Christian diaconal care and church mutual aid",
        "wordiness": "Short",
        "expected_root": "GROUPS",
        "expected_group": "Church Food Pantry Ministry",
        "stress_test_dimension": "Ministry collective mapped to GROUPS",
        "input_text": "Our church benevolence pantry is overwhelmed by pensioners unable to afford grocery and electricity bills. Praying for church members to give generously."
    },
    {
        "id": "REQ-066",
        "name": "National economic inflation and affordable housing crisis",
        "category_tag": "Workplace & Finances",
        "perspective": "Community housing advocate",
        "theology_focus": "Societal justice, stewardship, care for the poor",
        "wordiness": "Short",
        "expected_root": "GENERAL",
        "expected_group": null,
        "stress_test_dimension": "Macro-economic inflation and housing crisis mapped to GENERAL",
        "input_text": "Praying for government leaders and civil economists to establish just monetary policies that curb devastating inflation and provide affordable shelter for low-income families."
    },

    # =========================================================================
    # 10. FAMILY, PARENTING & MARRIAGE (8 items: 5 PEOPLE, 3 GROUPS)
    # =========================================================================
    {
        "id": "REQ-067",
        "name": "Marital estrangement and impending divorce papers",
        "category_tag": "Family & Marriage",
        "perspective": "Husband whose wife has moved out",
        "theology_focus": "Covenant faithfulness, humility, and biblical reconciliation",
        "wordiness": "Long",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Distilling marital heartbreak into objective petition for reconciliation",
        "input_text": "My wife Rachel packed her bags and took our two daughters to her mother's house on Tuesday. She says she has emotionally checked out and wants to see a divorce lawyer. I know I have been impatient, emotionally distant, and consumed with work, but I love her and want to fight for our covenant marriage. Praying for God to soften both of our hearts, break my pride, bring us into Christian marriage counseling, and reconcile our family according to His will."
    },
    {
        "id": "REQ-068",
        "name": "Exhausted young mother of toddlers",
        "category_tag": "Family & Marriage",
        "perspective": "Mother of three children under age four",
        "theology_focus": "Patience, gentleness, and daily sustaining grace",
        "wordiness": "Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Parenting strain under 'Me'",
        "input_text": "Surviving on three hours of broken sleep with a colicky infant and two tantruming toddlers. Praying for patience, self-control, and freedom from mom-guilt."
    },
    {
        "id": "REQ-069",
        "name": "Parenting shorthand",
        "category_tag": "Family & Marriage",
        "perspective": "Parent",
        "theology_focus": "Wisdom in Christian nurture",
        "wordiness": "Ultra-Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Ultra-short family request",
        "input_text": "Ethan ADHD school struggles patience"
    },
    {
        "id": "REQ-070",
        "name": "Aging parent dementia placement",
        "category_tag": "Family & Marriage",
        "perspective": "Adult son making agonizing care decision for mother",
        "theology_focus": "Honoring father and mother with dignity and wisdom",
        "wordiness": "Medium",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Elder care ethical dilemma mapped to PEOPLE (mother)",
        "input_text": "Mom's vascular dementia has reached the stage where she wanders out into the street at night and is unsafe alone. We have to move her into memory care this weekend. Heartbroken over her confusion and praying for peace for her and wisdom for our family."
    },
    {
        "id": "REQ-071",
        "name": "Foster child placement trauma",
        "category_tag": "Family & Marriage",
        "perspective": "Foster parents taking in traumatized siblings",
        "theology_focus": "Reflecting God's adoption of sinners; sacrificial love",
        "wordiness": "Medium",
        "expected_root": "GROUPS",
        "expected_group": "Foster Children Siblings",
        "stress_test_dimension": "Pair of siblings mapped to GROUPS",
        "input_text": "We just welcomed two foster siblings (ages 5 and 7) into our home who have suffered severe neglect and physical abuse. They are terrified and acting out in anger. Praying for God's gentleness to flow through us and for the children to feel safe and loved."
    },
    {
        "id": "REQ-072",
        "name": "Bitter inheritance feud among adult siblings",
        "category_tag": "Family & Marriage",
        "perspective": "Sibling witnessing family torn apart over will",
        "theology_focus": "Putting away greed, malice, and covetousness",
        "wordiness": "Medium",
        "expected_root": "GROUPS",
        "expected_group": "Adult Siblings",
        "stress_test_dimension": "Plural sibling group mapped to GROUPS",
        "input_text": "Since our father died last month, my two brothers and sister have hired lawyers over the estate and are hurling vile accusations at each other. Praying for the spirit of greed and bitterness to be broken, and for peace and fair settlement."
    },
    {
        "id": "REQ-073",
        "name": "Teenager cyberbullying and suicidal ideation",
        "category_tag": "Family & Marriage",
        "perspective": "Terrified father of 14-year-old daughter",
        "theology_focus": "Preservation of life and divine comfort in youth depression",
        "wordiness": "Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "High stakes youth mental health crisis",
        "input_text": "Our daughter Maya is being ruthlessly bullied on social media by classmates and confessed she wants to end her life. Praying for her protection, professional help, and for her to know her worth in God."
    },
    {
        "id": "REQ-074",
        "name": "Infertility heartbreak and silent tears",
        "category_tag": "Family & Marriage",
        "perspective": "Married couple after six years of unexplained infertility",
        "theology_focus": "Trust in God's good sovereignty when prayers for children seem unanswered",
        "wordiness": "Short",
        "expected_root": "GROUPS",
        "expected_group": "Husband and Wife",
        "stress_test_dimension": "Couple experiencing shared trial mapped to GROUPS",
        "input_text": "Another negative pregnancy test after six years of trying and IVF heartbreak. Praying for grace to trust God's sovereignty and protect our marriage from envy."
    },

    # =========================================================================
    # 11. PERSONAL SANCTIFICATION, REPENTANCE & MORTIFICATION OF SIN (8 items: 7 PEOPLE, 1 GENERAL)
    # =========================================================================
    {
        "id": "REQ-075",
        "name": "Hidden online pornography addiction",
        "category_tag": "Personal Sanctification",
        "perspective": "Young Christian man wrestling with habitual sexual sin",
        "theology_focus": "Confession, repentance, and mortification of the flesh (Rom 8:13)",
        "wordiness": "Medium",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Deep personal confession under 'Me' with zero therapy platitudes",
        "input_text": "I relapsed into viewing online pornography again last night after three months clean. I feel sick with hypocrisy and shame, especially serving on the church AV team. Asking God for godly sorrow leading to genuine repentance, courage to confess to my accountability partner, and Spirit-given self-control."
    },
    {
        "id": "REQ-076",
        "name": "Slander, gossip, and uncontrolled tongue",
        "category_tag": "Personal Sanctification",
        "perspective": "Church member convicted by James 3 sermon",
        "theology_focus": "Bridling the tongue and repenting of malicious speech",
        "wordiness": "Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Moral conviction under 'Me'",
        "input_text": "Convicted of participating in toxic gossip about our pastor's family at a dinner party. Praying for forgiveness, restraint over my tongue, and humility to apologize to those I slandered."
    },
    {
        "id": "REQ-077",
        "name": "Repentance shorthand",
        "category_tag": "Personal Sanctification",
        "perspective": "Humbled believer",
        "theology_focus": "Purity of heart and repentance",
        "wordiness": "Ultra-Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Ultra-short personal sanctification request",
        "input_text": "Pride anger repentance patience humility"
    },
    {
        "id": "REQ-078",
        "name": "Spiritual dryness, prayerlessness, and cold affections",
        "category_tag": "Personal Sanctification",
        "perspective": "Believer experiencing deep spiritual apathy",
        "theology_focus": "Renewal of first love by the Holy Spirit (Rev 2:4)",
        "wordiness": "Medium",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Devotional stagnation under 'Me'",
        "input_text": "My heart feels like concrete toward spiritual things. I have barely prayed or opened Scripture for two months and church feels like an empty chore. Praying that the Holy Spirit revives my soul, gives me a renewed hunger for Christ, and delivers me from worldly distractions."
    },
    {
        "id": "REQ-079",
        "name": "Resentment and envy toward successful peers",
        "category_tag": "Personal Sanctification",
        "perspective": "Christian feeling bitter over peers' promotions and homes",
        "theology_focus": "Contentment in Christ and eradication of envy",
        "wordiness": "Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Internal sin mortification under 'Me'",
        "input_text": "Consumed with jealousy every time I see friends buy big houses or get promoted. Praying that God uproots covetousness from my heart and teaches me godly contentment."
    },
    {
        "id": "REQ-080",
        "name": "Hypocrisy and double life at university",
        "category_tag": "Personal Sanctification",
        "perspective": "College student living a compromised lifestyle on weekends",
        "theology_focus": "Sincerity, single-minded devotion, and fear of God",
        "wordiness": "Medium",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Young adult double life conviction under 'Me'",
        "input_text": "Living a double life—devout Christian on Sundays, partying and drinking carelessly with my fraternity brothers on Friday nights. Deeply convicted of my cowardice and hypocrisy. Praying for courage to take a stand for Christ among my peers."
    },
    {
        "id": "REQ-081",
        "name": "Overcoming chronic deceit and small lies",
        "category_tag": "Personal Sanctification",
        "perspective": "Professional caught in habit of habitual exaggeration",
        "theology_focus": "Truthfulness in the inward parts (Psalm 51:6)",
        "wordiness": "Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Mortification of deceit under 'Me'",
        "input_text": "I catch myself habitually telling small lies to make myself look better or avoid minor blame at work. Praying for total honesty, humility, and fear of the Lord."
    },
    {
        "id": "REQ-082",
        "name": "Corporate repentance for secular compromise across denomination",
        "category_tag": "Personal Sanctification",
        "perspective": "Delegate to church assembly",
        "theology_focus": "Corporate confession and repentance (Nehemiah 1)",
        "wordiness": "Medium",
        "expected_root": "GENERAL",
        "expected_group": null,
        "stress_test_dimension": "Corporate confessional repentance mapped to GENERAL",
        "input_text": "Confessing our national church's silence in the face of secular moral decline, greed, and abandonment of the authority of Holy Scripture. Praying for widespread repentance among clergy and people."
    },

    # =========================================================================
    # 12. CHURCH MINISTRY, PREACHING & PASTORAL CARE (6 items: 2 PEOPLE, 3 GROUPS, 1 GENERAL)
    # =========================================================================
    {
        "id": "REQ-083",
        "name": "Solo pastor burnout in rural church plant",
        "category_tag": "Church & Ministry",
        "perspective": "Bi-vocational church planting pastor on the verge of quitting",
        "theology_focus": "Christ as the Chief Shepherd (1 Peter 5:4) sustaining weary under-shepherds",
        "wordiness": "Long",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Single pastor mapped to PEOPLE",
        "input_text": "I have been pastoring our rural church plant for five years while working 30 hours a week at a hardware store. We have only thirty members, giving is low, and two key families just left over minor disagreements about music. I am physically exhausted, emotionally drained, and tempting thoughts of resigning ministry keep flooding my mind. Praying for spiritual renewal for my soul, joy in preaching the Word this Sunday, and provision for my family so I don't collapse."
    },
    {
        "id": "REQ-084",
        "name": "Faithful expository preaching through Romans",
        "category_tag": "Church & Ministry",
        "perspective": "Senior pastor preparing new doctrinal sermon series",
        "theology_focus": "Uncompromising proclamation of the whole counsel of God (2 Tim 4:1-2)",
        "wordiness": "Medium",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Preacher pulpit ministry mapped to PEOPLE",
        "input_text": "Beginning a one-year expository preaching series through Paul's Epistle to the Romans next month. Praying for deep hermeneutical clarity, courage to preach difficult doctrines of election and justification without compromise, and that the Holy Spirit transforms our congregation."
    },
    {
        "id": "REQ-085",
        "name": "Elder board unity during difficult church discipline case",
        "category_tag": "Church & Ministry",
        "perspective": "Church elder facing polarizing discipline process",
        "theology_focus": "Purity of the Church and biblical restoration (Matt 18:15-17)",
        "wordiness": "Medium",
        "expected_root": "GROUPS",
        "expected_group": "Elder Board",
        "stress_test_dimension": "Elder board mapped to GROUPS",
        "input_text": "Our elders are meeting tonight to address an unrepentant member involved in public financial fraud. Two elders want to ignore it to avoid controversy, while others want biblical accountability. Praying for wisdom, humility, unwavering adherence to Scripture, and love for the sinner's soul."
    },
    {
        "id": "REQ-086",
        "name": "Sunday school teachers ministry",
        "category_tag": "Church & Ministry",
        "perspective": "Children's ministry director",
        "theology_focus": "Passing the Gospel faithfully to the next generation (Psalm 78:4)",
        "wordiness": "Short",
        "expected_root": "GROUPS",
        "expected_group": "Sunday School Teachers",
        "stress_test_dimension": "Teaching team mapped to GROUPS",
        "input_text": "Praying for our twelve volunteer Sunday school teachers ministering to sixty children this term. Praying for patience, love, and Gospel clarity."
    },
    {
        "id": "REQ-087",
        "name": "Church music and sound team tech rehearsal",
        "category_tag": "Church & Ministry",
        "perspective": "Worship director",
        "theology_focus": "Reverent worship in spirit and truth",
        "wordiness": "Short",
        "expected_root": "GROUPS",
        "expected_group": "Music and AV Team",
        "stress_test_dimension": "Worship tech volunteer team mapped to GROUPS",
        "input_text": "Praying for our volunteer musicians and audio technicians preparing for Sunday services. Praying for humble hearts free from performance pride."
    },
    {
        "id": "REQ-088",
        "name": "Global Anglican realignment and fidelity to historic formularies",
        "category_tag": "Church & Ministry",
        "perspective": "Anglican scholar and cleric",
        "theology_focus": "Historic Anglican formularies & 39 Articles fidelity",
        "wordiness": "Medium",
        "expected_root": "GENERAL",
        "expected_group": null,
        "stress_test_dimension": "Denominational realignment and confessional orthodoxy mapped to GENERAL",
        "input_text": "Praying for the global fellowship of confessional Anglicans (GAFCON and Global South Fellowship). Praying that bishops stand unwavering on the authority of Holy Scripture and the historic 1662 Book of Common Prayer."
    },

    # =========================================================================
    # 13. CIVIL GOVERNANCE, JUSTICE & NATION (6 items: 1 PEOPLE, 1 GROUPS, 4 GENERAL)
    # =========================================================================
    {
        "id": "REQ-089",
        "name": "National Prime Minister and Cabinet wisdom",
        "category_tag": "Civil Governance & Nation",
        "perspective": "Citizen obedient to 1 Timothy 2:1-2",
        "theology_focus": "Prayer for civil magistrates to rule justly and preserve peace",
        "wordiness": "Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Single civil magistrate leader mapped to PEOPLE",
        "input_text": "Praying for the Prime Minister as difficult budget and defense decisions are made this week. Praying for moral integrity and policies that promote justice."
    },
    {
        "id": "REQ-090",
        "name": "Parliamentary joint committee on religious freedom",
        "category_tag": "Civil Governance & Nation",
        "perspective": "Christian legal advocate",
        "theology_focus": "Freedom to preach the Gospel without civil coercion",
        "wordiness": "Short",
        "expected_root": "GROUPS",
        "expected_group": "Parliamentary Committee",
        "stress_test_dimension": "Legislative committee mapped to GROUPS",
        "input_text": "Praying for the parliamentary committee reviewing religious exemptions for faith-based schools and charities. Praying for wisdom and protection of religious conscience."
    },
    {
        "id": "REQ-091",
        "name": "Protection of the unborn and sanctity of human life",
        "category_tag": "Civil Governance & Nation",
        "perspective": "Crisis pregnancy counselor",
        "theology_focus": "Sanctity of human life created in the image of God",
        "wordiness": "Short",
        "expected_root": "GENERAL",
        "expected_group": null,
        "stress_test_dimension": "Broad ethical/sanctity of life topic mapped to GENERAL",
        "input_text": "Praying for our society to cherish and protect unborn children, for women in crisis pregnancies to receive compassionate support, and for an end to abortion."
    },
    {
        "id": "REQ-092",
        "name": "Integrity of judiciary and constitutional rule of law",
        "category_tag": "Civil Governance & Nation",
        "perspective": "Constitutional lawyer",
        "theology_focus": "God as the supreme Judge; righteous civil justice",
        "wordiness": "Short",
        "expected_root": "GENERAL",
        "expected_group": null,
        "stress_test_dimension": "Judicial system and constitutional order mapped to GENERAL",
        "input_text": "Praying for judges and magistrates to execute impartial justice without bribery, corruption, or ideological bias, defending the innocent and punishing evil."
    },
    {
        "id": "REQ-093",
        "name": "Relief from severe regional drought across farming communities",
        "category_tag": "Civil Governance & Nation",
        "perspective": "Rural agricultural worker",
        "theology_focus": "God as sovereign provider of rain in due season",
        "wordiness": "Short",
        "expected_root": "GENERAL",
        "expected_group": null,
        "stress_test_dimension": "Environmental/agricultural climate crisis mapped to GENERAL",
        "input_text": "Praying for replenishing rains across drought-stricken farming districts, relief for bankrupt farmers, and preservation of livestock and harvests."
    },
    {
        "id": "REQ-094",
        "name": "Peace in the Middle East and protection of civilians in conflict zones",
        "category_tag": "Civil Governance & Nation",
        "perspective": "Humanitarian worker",
        "theology_focus": "Sovereign peace; restraining bloodshed; Gospel as ultimate reconciliation",
        "wordiness": "Medium",
        "expected_root": "GENERAL",
        "expected_group": null,
        "stress_test_dimension": "Geopolitical conflict and civilian preservation mapped to GENERAL",
        "input_text": "Praying for an end to bloodshed in the Middle East, safe delivery of humanitarian aid to traumatized civilians, protection for peacemakers, and for the Prince of Peace to make Himself known to Arabs and Israelis alike."
    },

    # =========================================================================
    # 14. THANKSGIVING, ADORATION & HISTORIC PRAISE (5 items: 2 PEOPLE, 1 GROUPS, 2 GENERAL)
    # =========================================================================
    {
        "id": "REQ-095",
        "name": "Thanksgiving for healthy newborn after recurrent miscarriages",
        "category_tag": "Thanksgiving & Praise",
        "perspective": "Overjoyed father holding first living child",
        "theology_focus": "Soli Deo Gloria & humble praise for divine goodness",
        "wordiness": "Medium",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Pure thanksgiving distillation into praise petition",
        "input_text": "After three heartbreaking miscarriages, our daughter Evelyn was born healthy and vigorous at 8 lbs this morning! Praising God with all our hearts for His tender mercy, protecting my wife during delivery, and praying we raise this little girl in the fear and admonition of the Lord."
    },
    {
        "id": "REQ-096",
        "name": "Thanksgiving shorthand",
        "category_tag": "Thanksgiving & Praise",
        "perspective": "Grateful believer",
        "theology_focus": "Soli Deo Gloria",
        "wordiness": "Ultra-Short",
        "expected_root": "GENERAL",
        "expected_group": null,
        "stress_test_dimension": "Ultra-short praise note",
        "input_text": "Praising God cancer remission confirmed today"
    },
    {
        "id": "REQ-097",
        "name": "Miraculous provision after six months of unemployment",
        "category_tag": "Thanksgiving & Praise",
        "perspective": "Father who received surprise job offer on verge of eviction",
        "theology_focus": "Jehovah Jireh; God's faithful providence to His children",
        "wordiness": "Short",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Thanksgiving for financial provision under 'Me'",
        "input_text": "Offered a full-time senior engineering job today after six months of joblessness and rejections. Thanking the Lord for His faithful provision and sustaining our faith through poverty."
    },
    {
        "id": "REQ-098",
        "name": "Thanksgiving for fifty years of faithful Gospel preaching in local parish",
        "category_tag": "Thanksgiving & Praise",
        "perspective": "Lifelong parishioner celebrating church jubilee",
        "theology_focus": "Faithfulness of God across generations (Psalm 100:5)",
        "wordiness": "Short",
        "expected_root": "GROUPS",
        "expected_group": "Local Parish Congregation",
        "stress_test_dimension": "Corporate parish jubilee mapped to GROUPS",
        "input_text": "Thanking God for our church's 50th anniversary celebration this Sunday. Praising Him for half a century of unbroken biblical preaching and praying for continued Gospel fidelity."
    },
    {
        "id": "REQ-099",
        "name": "General collect of adoration for God's eternal sovereignty",
        "category_tag": "Thanksgiving & Praise",
        "perspective": "Devout believer meditating on Psalm 103 and 1662 BCP collect",
        "theology_focus": "Soli Deo Gloria; transcendent majesty of the Holy Trinity",
        "wordiness": "Medium",
        "expected_root": "GENERAL",
        "expected_group": null,
        "stress_test_dimension": "High liturgical adoration mapped to GENERAL root",
        "input_text": "Almighty and everlasting God, who art always more ready to hear than we to pray, we adore Thee for Thine eternal glory, Thy providential rule over heaven and earth, and the redemption of the world by our Lord Jesus Christ."
    },

    # =========================================================================
    # 15. EXTREME WALL OF TEXT / CHAOTIC STREAM OF CONSCIOUSNESS (1 item to reach 100)
    # Testing maximum token digestion, extraction of single core burden, and brevity constraint
    # =========================================================================
    {
        "id": "REQ-100",
        "name": "300-word rambling stream-of-consciousness family financial meltdown",
        "category_tag": "Chaotic Stream of Consciousness",
        "perspective": "Overwhelmed parent venting complete life crisis in one breathless paragraph",
        "theology_focus": "Separating chaotic panic into a focused biblical petition submitted to God's will",
        "wordiness": "Extreme / Wall of Text",
        "expected_root": "PEOPLE",
        "expected_group": null,
        "stress_test_dimension": "Extreme token load and multi-issue extraction into strictly 1 core burden and 2 concise cards",
        "input_text": "I do not even know where to begin because everything is going wrong all at the same time and I can barely breathe. My mother-in-law was admitted to the hospital three days ago with congestive heart failure and she has no medical insurance so my husband and I are going to have to pay out of pocket for her specialist bills but our car transmission also just blew up on the freeway on Wednesday and the mechanic quoted us thirty-five hundred dollars which we don't have because I was put on reduced hours at the dental clinic and on top of that our fourteen-year-old son Lucas has completely shut down at school, getting in fights, failing algebra, refusing to speak to us at dinner, and spending all night on his phone in his room. My husband has been sleeping on the couch for two weeks because we got into a massive screaming match over money and how to handle his mother and I feel like our marriage is crumbling to dust while everyone else at church seems to have these perfect peaceful lives and holy marriages. I am having chest palpitations every morning when I wake up and I haven't been able to pray or read my Bible because every time I try I just start crying hysterically and feel like God has completely abandoned our family to the wolves. Please just help me find some peace and make this nightmare stop because I cannot carry all of this on my own anymore."
    }
]

def analyze_dataset(items):
    total = len(items)
    print(f"Total Test Cases: {total}")
    assert total == 100, f"Expected exactly 100 items, got {total}"

    ids = [x["id"] for x in items]
    assert len(ids) == len(set(ids)), "Duplicate IDs found!"

    # Word count distribution
    word_counts = []
    roots = {}
    categories = {}
    wordiness_tiers = {}

    for item in items:
        text = item["input_text"]
        wc = len(text.split())
        item["word_count"] = wc
        word_counts.append(wc)

        r = item["expected_root"]
        roots[r] = roots.get(r, 0) + 1

        c = item["category_tag"]
        categories[c] = categories.get(c, 0) + 1

        w = item["wordiness"]
        wordiness_tiers[w] = wordiness_tiers.get(w, 0) + 1

    print("\n--- Root Category Distribution ---")
    for r, count in sorted(roots.items()):
        print(f"  {r}: {count} ({count/total*100:.1f}%)")

    print("\n--- Category Tag Distribution ---")
    for c, count in sorted(categories.items()):
        print(f"  {c}: {count}")

    print("\n--- Wordiness Tier Distribution ---")
    for w, count in sorted(wordiness_tiers.items()):
        print(f"  {w}: {count}")

    print(f"\n--- Word Count Stats ---")
    print(f"  Min Words: {min(word_counts)}")
    print(f"  Max Words: {max(word_counts)}")
    print(f"  Avg Words: {sum(word_counts) / len(word_counts):.1f}")

def main():
    target_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "prayer_requests_stress_test.json")
    analyze_dataset(TEST_CASES)

    with open(target_path, "w", encoding="utf-8") as f:
        json.dump(TEST_CASES, f, indent=2, ensure_ascii=False)

    print(f"\nSuccessfully wrote 100 stress-test cases to: {target_path}")

if __name__ == "__main__":
    main()
