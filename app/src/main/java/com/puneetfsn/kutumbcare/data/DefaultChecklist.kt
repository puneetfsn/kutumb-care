package com.puneetfsn.kutumbcare.data

/**
 * The 24 seeded India-specific records, in English and Hindi.
 * [HINDI_BY_ORDER] lets the Room 1 → 2 migration fill the new Hindi columns
 * for families who already have the English-only database.
 */
object DefaultChecklist {

    /** sortOrder → (Hindi title, Hindi description). */
    val HINDI_BY_ORDER: Map<Int, Pair<String, String>> = mapOf(
        10 to ("आधार कार्ड" to "आधार की प्रतियाँ, जुड़ा मोबाइल नंबर और DigiLocker की जानकारी। यहाँ OTP या आधार PIN न लिखें।"),
        20 to ("PAN कार्ड" to "PAN नंबर का स्थान और क्या स्कैन की गई प्रति आधार फ़ाइल के साथ है।"),
        30 to ("पासपोर्ट, मतदाता पहचान पत्र और ड्राइविंग लाइसेंस" to "पासपोर्ट, मतदाता पहचान पत्र और DL — मूल और प्रतियाँ कहाँ रखी हैं।"),
        40 to ("वसीयत और निष्पादक" to "पंजीकृत या अपंजीकृत वसीयत, निष्पादक का नाम, और वह वकील या संरक्षक जिसके पास प्रति है।"),
        50 to ("बचत और चालू खाते" to "बैंक का नाम, शाखा, खाते का प्रकार और नॉमिनी दर्ज है या नहीं। खाता नंबर केवल ज़रूरत पर लिखें — नेट-बैंकिंग पासवर्ड या UPI PIN कभी नहीं।"),
        60 to ("स्थायी जमा और आवर्ती जमा" to "FD / RD रसीदें, जारी करने वाला बैंक, परिपक्वता नोट और नॉमिनी की स्थिति।"),
        70 to ("बैंक लॉकर" to "लॉकर नंबर, शाखा, संयुक्त धारक और लॉकर की चाबी कहाँ रखी है।"),
        80 to ("क्रेडिट कार्ड" to "जारी करने वाला बैंक और बकाया राशि जाँचने की याद। CVV, ATM PIN या OTP कभी न रखें।"),
        90 to ("डीमैट और म्यूचुअल फंड" to "Zerodha, Groww, Angel One, CAMS या KFintech फोलियो। DP ID / क्लाइंट ID और नॉमिनी स्थिति लिखें — ब्रोकर पासवर्ड कभी नहीं।"),
        100 to ("EPF / UAN" to "EPF निकासी और EDLI जीवन बीमा के लिए यूनिवर्सल खाता नंबर। EPFO पोर्टल पर नॉमिनी अद्यतन है, यह सुनिश्चित करें।"),
        110 to ("PPF पासबुक" to "सार्वजनिक भविष्य निधि खाता, बैंक या डाकघर शाखा, पासबुक का स्थान और नॉमिनी।"),
        120 to ("NPS / PRAN" to "स्थायी सेवानिवृत्ति खाता नंबर, POP या नियोक्ता NPS, और नॉमिनी / पारिवारिक पेंशन विवरण।"),
        130 to ("भौतिक सोना और सॉवरेन गोल्ड बॉन्ड" to "SGB प्रमाणपत्र, जौहरी रसीदें, लॉकर पर्चियाँ और भौतिक सोना कहाँ रखा है।"),
        140 to ("टर्म जीवन / LIC पॉलिसियाँ" to "बीमाकर्ता, पॉलिसी नंबर, बीमित राशि, नॉमिनी और दावा शुरू करने वाला एजेंट।"),
        150 to ("स्वास्थ्य बीमा / मेडिक्लेम" to "पारिवारिक फ्लोटर या व्यक्तिगत पॉलिसी, TPA कार्ड, पॉलिसी नंबर और कैशलेस अस्पताल नोट।"),
        160 to ("वाहन बीमा और RC" to "पंजीकरण प्रमाणपत्र, बीमा काग़ज़ात और अतिरिक्त चाबी कहाँ रखी है।"),
        170 to ("संपत्ति के काग़ज़ात और खाता" to "मूल रजिस्ट्री, भार-मुक्ति प्रमाणपत्र, खाता / पट्टा / 7-12 उतारा और संपत्ति कर रसीदें।"),
        180 to ("गृह ऋण और क्रेडिट-शील्ड कवर" to "आवास या कार ऋण के काग़ज़, ऋणदाता, बकाया जाँचने की याद, और ऋण से जुड़ा जीवन बीमा है या नहीं।"),
        190 to ("फ़ोन अनलॉक नोट का स्थान" to "आपातकाल में भरोसेमंद व्यक्ति को लिखा हुआ स्क्रीन-अनलॉक नोट कहाँ मिलेगा। PIN स्वयं इस ऐप में न लिखें।"),
        200 to ("मुख्य रिकवरी ईमेल" to "बैंक और बीमा के लिए मुख्य Gmail / Apple ID। Google Inactive Account Manager या Apple Legacy Contact सेट है या नहीं लिखें। पासवर्ड कभी न रखें।"),
        210 to ("DigiLocker नोट का स्थान" to "DigiLocker मोबाइल / सुरक्षा नोट कहाँ रखा है — PIN या आधार OTP नहीं।"),
        220 to ("पारिवारिक CA / कर सलाहकार" to "वह चार्टर्ड अकाउंटेंट जो ITR, फ़ॉर्म 16 और निवेश प्रमाण जानता है।"),
        230 to ("पारिवारिक वकील / अधिवक्ता" to "वह अधिवक्ता जिसने वसीयत, मुख्तारनामा या संपत्ति के काग़ज़ बनाए।"),
        240 to ("LIC / बीमा एजेंट" to "वह एजेंट या रिलेशनशिप मैनेजर जो जीवन या स्वास्थ्य दावा दाखिल कर सकता है।")
    )

    val ITEMS: List<ChecklistItem> = listOf(
        item(
            AssetCategory.IDENTITY_GOVT, 10,
            "Aadhaar card",
            "Physical Aadhaar copies, linked mobile number note, and DigiLocker availability. Do not write OTP or Aadhaar OTP PIN here.",
            "आधार कार्ड",
            "आधार की प्रतियाँ, जुड़ा मोबाइल नंबर और DigiLocker की जानकारी। यहाँ OTP या आधार PIN न लिखें।"
        ),
        item(
            AssetCategory.IDENTITY_GOVT, 20,
            "PAN card",
            "PAN number location and whether a scanned copy sits with the Aadhaar file.",
            "PAN कार्ड",
            "PAN नंबर का स्थान और क्या स्कैन की गई प्रति आधार फ़ाइल के साथ है।"
        ),
        item(
            AssetCategory.IDENTITY_GOVT, 30,
            "Passport, voter ID & driving licence",
            "Passport number booklet, voter ID and DL — where the originals and photocopies are kept.",
            "पासपोर्ट, मतदाता पहचान पत्र और ड्राइविंग लाइसेंस",
            "पासपोर्ट, मतदाता पहचान पत्र और DL — मूल और प्रतियाँ कहाँ रखी हैं।"
        ),
        item(
            AssetCategory.IDENTITY_GOVT, 40,
            "Will & executor",
            "Registered or unregistered will, executor name, and the advocate or custodian who holds a copy.",
            "वसीयत और निष्पादक",
            "पंजीकृत या अपंजीकृत वसीयत, निष्पादक का नाम, और वह वकील या संरक्षक जिसके पास प्रति है।", NomineeStatus.NOT_APPLICABLE
        ),
        item(
            AssetCategory.BANKING_FUNDS, 50,
            "Savings & current accounts",
            "Bank name, branch, account type and whether a nominee is registered. Record account numbers only if useful — never net-banking passwords or UPI PINs.",
            "बचत और चालू खाते",
            "बैंक का नाम, शाखा, खाते का प्रकार और नॉमिनी दर्ज है या नहीं। खाता नंबर केवल ज़रूरत पर लिखें — नेट-बैंकिंग पासवर्ड या UPI PIN कभी नहीं।"
        ),
        item(
            AssetCategory.BANKING_FUNDS, 60,
            "Fixed deposits & recurring deposits",
            "FD / RD receipts, issuing bank, maturity notes and nominee status.",
            "स्थायी जमा और आवर्ती जमा",
            "FD / RD रसीदें, जारी करने वाला बैंक, परिपक्वता नोट और नॉमिनी की स्थिति।"
        ),
        item(
            AssetCategory.BANKING_FUNDS, 70,
            "Bank locker",
            "Locker number, branch, joint holder, and the physical location of the locker key.",
            "बैंक लॉकर",
            "लॉकर नंबर, शाखा, संयुक्त धारक और लॉकर की चाबी कहाँ रखी है।"
        ),
        item(
            AssetCategory.BANKING_FUNDS, 80,
            "Credit cards",
            "Issuing bank and a reminder to check outstanding balances. Never store CVV, ATM PIN or OTP.",
            "क्रेडिट कार्ड",
            "जारी करने वाला बैंक और बकाया राशि जाँचने की याद। CVV, ATM PIN या OTP कभी न रखें।"
        ),
        item(
            AssetCategory.INVESTMENTS, 90,
            "Demat & mutual funds",
            "Zerodha, Groww, Angel One, CAMS or KFintech folios. Note DP ID / client ID and nominee status — never broker passwords.",
            "डीमैट और म्यूचुअल फंड",
            "Zerodha, Groww, Angel One, CAMS या KFintech फोलियो। DP ID / क्लाइंट ID और नॉमिनी स्थिति लिखें — ब्रोकर पासवर्ड कभी नहीं।"
        ),
        item(
            AssetCategory.INVESTMENTS, 100,
            "EPF / UAN",
            "Universal Account Number for EPF withdrawal and the EDLI life cover. Confirm nominee on the EPFO portal is current.",
            "EPF / UAN",
            "EPF निकासी और EDLI जीवन बीमा के लिए यूनिवर्सल खाता नंबर। EPFO पोर्टल पर नॉमिनी अद्यतन है, यह सुनिश्चित करें।"
        ),
        item(
            AssetCategory.INVESTMENTS, 110,
            "PPF passbook",
            "Public Provident Fund account, bank or post office branch, passbook location and nominee.",
            "PPF पासबुक",
            "सार्वजनिक भविष्य निधि खाता, बैंक या डाकघर शाखा, पासबुक का स्थान और नॉमिनी।"
        ),
        item(
            AssetCategory.INVESTMENTS, 120,
            "NPS / PRAN",
            "Permanent Retirement Account Number, POP or employer NPS, and nominee / family pension details.",
            "NPS / PRAN",
            "स्थायी सेवानिवृत्ति खाता नंबर, POP या नियोक्ता NPS, और नॉमिनी / पारिवारिक पेंशन विवरण।"
        ),
        item(
            AssetCategory.INVESTMENTS, 130,
            "Physical gold & sovereign gold bonds",
            "SGB certificates, jeweller receipts, locker slips and where the physical gold is stored.",
            "भौतिक सोना और सॉवरेन गोल्ड बॉन्ड",
            "SGB प्रमाणपत्र, जौहरी रसीदें, लॉकर पर्चियाँ और भौतिक सोना कहाँ रखा है।"
        ),
        item(
            AssetCategory.INSURANCE, 140,
            "Term life / LIC policies",
            "Insurer, policy number, sum assured, nominee and the agent who can start a claim.",
            "टर्म जीवन / LIC पॉलिसियाँ",
            "बीमाकर्ता, पॉलिसी नंबर, बीमित राशि, नॉमिनी और दावा शुरू करने वाला एजेंट।"
        ),
        item(
            AssetCategory.INSURANCE, 150,
            "Health insurance / mediclaim",
            "Family floater or individual policy, TPA card, policy number and cashless hospital notes.",
            "स्वास्थ्य बीमा / मेडिक्लेम",
            "पारिवारिक फ्लोटर या व्यक्तिगत पॉलिसी, TPA कार्ड, पॉलिसी नंबर और कैशलेस अस्पताल नोट।"
        ),
        item(
            AssetCategory.INSURANCE, 160,
            "Vehicle insurance & RC",
            "Registration certificates, insurance papers and where the spare key is kept.",
            "वाहन बीमा और RC",
            "पंजीकरण प्रमाणपत्र, बीमा काग़ज़ात और अतिरिक्त चाबी कहाँ रखी है।"
        ),
        item(
            AssetCategory.REAL_ESTATE_LOANS, 170,
            "Property deeds & khata",
            "Original registry, encumbrance certificate, khata / patta / 7-12 extract and property tax receipts.",
            "संपत्ति के काग़ज़ात और खाता",
            "मूल रजिस्ट्री, भार-मुक्ति प्रमाणपत्र, खाता / पट्टा / 7-12 उतारा और संपत्ति कर रसीदें।"
        ),
        item(
            AssetCategory.REAL_ESTATE_LOANS, 180,
            "Home loan & credit-shield cover",
            "Housing or car loan papers, lender, outstanding-check reminder, and whether a loan-linked life cover exists.",
            "गृह ऋण और क्रेडिट-शील्ड कवर",
            "आवास या कार ऋण के काग़ज़, ऋणदाता, बकाया जाँचने की याद, और ऋण से जुड़ा जीवन बीमा है या नहीं।", NomineeStatus.NEEDS_NOMINEE
        ),
        item(
            AssetCategory.DIGITAL_LEGACY, 190,
            "Phone unlock note location",
            "Where a trusted person can find the written screen-unlock note in an emergency. Do not type the PIN itself into this app.",
            "फ़ोन अनलॉक नोट का स्थान",
            "आपातकाल में भरोसेमंद व्यक्ति को लिखा हुआ स्क्रीन-अनलॉक नोट कहाँ मिलेगा। PIN स्वयं इस ऐप में न लिखें।", NomineeStatus.NOT_APPLICABLE
        ),
        item(
            AssetCategory.DIGITAL_LEGACY, 200,
            "Primary recovery email",
            "Main Gmail / Apple ID used for banks and insurance. Note whether Google Inactive Account Manager or Apple Legacy Contact is set. Never store the password.",
            "मुख्य रिकवरी ईमेल",
            "बैंक और बीमा के लिए मुख्य Gmail / Apple ID। Google Inactive Account Manager या Apple Legacy Contact सेट है या नहीं लिखें। पासवर्ड कभी न रखें।", NomineeStatus.NOT_APPLICABLE
        ),
        item(
            AssetCategory.DIGITAL_LEGACY, 210,
            "DigiLocker note location",
            "Where the DigiLocker mobile / security note is kept — not the PIN or Aadhaar OTP.",
            "DigiLocker नोट का स्थान",
            "DigiLocker मोबाइल / सुरक्षा नोट कहाँ रखा है — PIN या आधार OTP नहीं।", NomineeStatus.NOT_APPLICABLE
        ),
        item(
            AssetCategory.TRUSTED_CONTACTS, 220,
            "Family CA / tax consultant",
            "Chartered accountant who knows the ITRs, Form 16s and investment proofs.",
            "पारिवारिक CA / कर सलाहकार",
            "वह चार्टर्ड अकाउंटेंट जो ITR, फ़ॉर्म 16 और निवेश प्रमाण जानता है।", NomineeStatus.NOT_APPLICABLE
        ),
        item(
            AssetCategory.TRUSTED_CONTACTS, 230,
            "Family lawyer / advocate",
            "Advocate who drafted the will, power of attorney or property papers.",
            "पारिवारिक वकील / अधिवक्ता",
            "वह अधिवक्ता जिसने वसीयत, मुख्तारनामा या संपत्ति के काग़ज़ बनाए।", NomineeStatus.NOT_APPLICABLE
        ),
        item(
            AssetCategory.TRUSTED_CONTACTS, 240,
            "LIC / insurance agent",
            "The agent or relationship manager who can file a life or health claim.",
            "LIC / बीमा एजेंट",
            "वह एजेंट या रिलेशनशिप मैनेजर जो जीवन या स्वास्थ्य दावा दाखिल कर सकता है।", NomineeStatus.NOT_APPLICABLE
        )

        item(
            AssetCategory.DIGITAL_LEGACY, 250,
            "Primary Gmail / Google account",
            "Email ID, registered mobile, recovery email, and whether Google's Inactive Account Manager is set with a trusted contact. Never store the password — note where it is kept.",
            "मुख्य Gmail / Google खाता",
            "ईमेल ID, पंजीकृत मोबाइल, रिकवरी ईमेल, और क्या Google का Inactive Account Manager भरोसेमंद संपर्क के साथ सेट है। पासवर्ड कभी न रखें — वह कहाँ रखा है, यह लिखें।",
            NomineeStatus.NOT_APPLICABLE
        ),
        item(
            AssetCategory.DIGITAL_LEGACY, 260,
            "Apple ID",
            "Apple ID email, registered mobile, recovery email, and whether a Legacy Contact is set. Their access key location matters — Keychain passwords are never shared by Apple.",
            "Apple ID",
            "Apple ID ईमेल, पंजीकृत मोबाइल, रिकवरी ईमेल, और क्या Legacy Contact सेट है। उनकी एक्सेस कुंजी का स्थान मायने रखता है — Apple Keychain पासवर्ड कभी साझा नहीं करता।",
            NomineeStatus.NOT_APPLICABLE
        ),
        item(
            AssetCategory.DIGITAL_LEGACY, 270,
            "Other email accounts",
            "Outlook, Yahoo, Rediff or old Gmail IDs that may guard bank and tax accounts — email ID, registered mobile and recovery email for each. Never the passwords.",
            "अन्य ईमेल खाते",
            "Outlook, Yahoo, Rediff या पुरानी Gmail IDs जो बैंक और कर खातों की रक्षा कर सकती हैं — हर एक का ईमेल ID, पंजीकृत मोबाइल और रिकवरी ईमेल। पासवर्ड कभी नहीं।",
            NomineeStatus.NOT_APPLICABLE
        ),
        item(
            AssetCategory.DIGITAL_LEGACY, 280,
            "Income Tax (ITR) account",
            "Portal username (usually PAN), registered mobile and email — needed to file the final return. Password stays in the password manager; note where.",
            "आयकर (ITR) खाता",
            "पोर्टल उपयोगकर्ता नाम (आमतौर पर PAN), पंजीकृत मोबाइल और ईमेल — अंतिम रिटर्न दाखिल करने के लिए चाहिए। पासवर्ड पासवर्ड मैनेजर में रहता है; कहाँ है, लिखें।",
            NomineeStatus.NOT_APPLICABLE
        ),
        item(
            AssetCategory.DIGITAL_LEGACY, 290,
            "Password manager",
            "Bitwarden, 1Password or KeePass — vault email, emergency-access contact, and where the master-password hint or sealed copy is kept. This is the safe way to hand over every login.",
            "पासवर्ड मैनेजर",
            "Bitwarden, 1Password या KeePass — वॉल्ट ईमेल, आपातकालीन पहुँच संपर्क, और मास्टर-पासवर्ड संकेत या मुहरबंद प्रति कहाँ रखी है। हर लॉगिन सौंपने का सुरक्षित तरीका यही है।",
            NomineeStatus.NOT_APPLICABLE
        ),
        item(
            AssetCategory.DIGITAL_LEGACY, 300,
            "Govt portals & official logins",
            "EPFO, bank, Demat and other official portals — portal name, username/client ID and registered mobile for each, so family can use official recovery and claim routes. Never the passwords.",
            "सरकारी पोर्टल और आधिकारिक लॉगिन",
            "EPFO, बैंक, डीमैट और अन्य आधिकारिक पोर्टल — हर एक का पोर्टल नाम, उपयोगकर्ता नाम/क्लाइंट ID और पंजीकृत मोबाइल, ताकि परिवार आधिकारिक रिकवरी और दावा मार्ग उपयोग कर सके। पासवर्ड कभी नहीं।",
            NomineeStatus.NOT_APPLICABLE
        ),
        item(
            AssetCategory.DIGITAL_LEGACY, 310,
            "Social & messaging accounts",
            "WhatsApp, Facebook, Instagram — username, registered mobile, and legacy/memorial contact where the platform offers one. Never the passwords.",
            "सोशल और मैसेजिंग खाते",
            "WhatsApp, Facebook, Instagram — उपयोगकर्ता नाम, पंजीकृत मोबाइल, और जहाँ प्लेटफ़ॉर्म देता है वहाँ विरासत/स्मारक संपर्क। पासवर्ड कभी नहीं।",
            NomineeStatus.NOT_APPLICABLE
        )
    )
    )

    private fun item(
        category: AssetCategory,
        order: Int,
        title: String,
        description: String,
        titleHi: String,
        descriptionHi: String,
        nominee: NomineeStatus = if (category.requiresNominee) NomineeStatus.NEEDS_NOMINEE else NomineeStatus.NOT_APPLICABLE
    ) = ChecklistItem(
        category = category.name,
        title = title,
        description = description,
        titleHi = titleHi,
        descriptionHi = descriptionHi,
        nomineeStatus = nominee.name,
        sortOrder = order
    )
}
