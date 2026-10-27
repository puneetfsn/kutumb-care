package com.puneetfsn.kutumbcare.data

/** Label for one storage field, in both languages. Placeholders are examples, never data. */
data class EntryFieldLabel(
    val en: String,
    val hi: String,
    val phEn: String = "",
    val phHi: String = ""
) {
    fun label(hindi: Boolean) = if (hindi) hi else en
    fun placeholder(hindi: Boolean) = if (hindi) phHi else phEn
}

/**
 * Which fields a record actually asks for, and what they are called.
 *
 * The storage columns are generic (institutionName, accountRef, …); this
 * catalogue gives each seeded record its own meaning for them, so an Aadhaar
 * card never asks for a "branch", and a Gmail account asks where the password
 * is *kept* — never the password itself.
 *
 * Field choices follow official guidance:
 * - Aadhaar / PAN: this dossier is the family's own sealed copy, so it holds
 *   the full numbers (forms and logins need them) — still never an OTP or PIN.
 * - SEBI (2026 nomination framework): a nomination needs the nominee's name
 *   and relationship; share %, mobile/email and an ID are optional.
 * - EPFO e-nomination: nominees are family members, shares must total 100%,
 *   a minor nominee needs a guardian.
 * - IRDAI (Protection of Policyholders' Interests, 2024): nominee name,
 *   relationship, mobile/email; appointee for a minor nominee.
 * - Google Inactive Account Manager: up to 10 trusted contacts, an inactivity
 *   period of 3–18 months, contacts verified by phone (support.google.com).
 * - Apple Legacy Contact: the contact needs the generated access key plus a
 *   death certificate; Keychain passwords are never shared (support.apple.com).
 */
data class EntryFields(
    val primary: EntryFieldLabel?,
    val ref: EntryFieldLabel?,
    val location: EntryFieldLabel?,
    val contact: EntryFieldLabel?,
    val phone: EntryFieldLabel?,
    val notes: EntryFieldLabel?,
    val nomineeRelevant: Boolean,
    val guidanceEn: String,
    val guidanceHi: String,
    /** Label for the record's maturity/expiry date, when the record has one. */
    val maturity: EntryFieldLabel? = null
) {
    fun guidance(hindi: Boolean) = if (hindi) guidanceHi else guidanceEn
}

private fun f(en: String, hi: String, phEn: String = "", phHi: String = "") =
    EntryFieldLabel(en, hi, phEn, phHi)

private val NOTES = f("Notes for the family", "परिवार के लिए नोट")
private val LOCATION_DOCS = f(
    "Where are the documents kept?", "दस्तावेज़ कहाँ रखे हैं?",
    "Master bedroom almirah / blue folder", "मुख्य शयनकक्ष अलमारी / नीला फ़ोल्डर"
)
private val PASSWORD_KEPT_AT = f(
    "Password kept at (never write the password)", "पासवर्ड कहाँ रखा है (पासवर्ड कभी न लिखें)",
    "Password manager / sealed envelope in locker", "पासवर्ड मैनेजर / लॉकर में मुहरबंद लिफ़ाफ़ा"
)
private val REGISTERED_MOBILE = f("Registered mobile number", "पंजीकृत मोबाइल नंबर", "98XXXXXXXX", "98XXXXXXXX")
private val NOMINEE_NAME = f("Nominee name", "नॉमिनी का नाम")
private val NOMINEE_PHONE = f("Nominee / contact phone", "नॉमिनी / संपर्क फ़ोन")

object EntryFieldsCatalog {

    private val EDUCATION_FIELDS = EntryFields(
        primary = f("Board / university / institution", "बोर्ड / विश्वविद्यालय / संस्थान", "CBSE / state board / university", "CBSE / राज्य बोर्ड / विश्वविद्यालय"),
        ref = f("Roll / enrolment number", "रोल / नामांकन नंबर"),
        location = LOCATION_DOCS,
        contact = f("School / college office", "स्कूल / कॉलेज कार्यालय"),
        phone = f("Contact phone", "संपर्क फ़ोन"),
        notes = NOTES, nomineeRelevant = false,
        guidanceEn = "Keep the originals together and attach a clear photo of each certificate — boards and universities ask for these for jobs, passports and further study.",
        guidanceHi = "मूल दस्तावेज़ एक साथ रखें और हर प्रमाणपत्र की स्पष्ट फ़ोटो लगाएँ — नौकरी, पासपोर्ट और आगे की पढ़ाई के लिए बोर्ड व विश्वविद्यालय यही माँगते हैं।"
    )

    private val MEDICAL_FIELDS = EntryFields(
        primary = f("Hospital / clinic / doctor", "अस्पताल / क्लीनिक / डॉक्टर"),
        ref = f("Health ID / file number (ABHA, UHID)", "स्वास्थ्य ID / फ़ाइल नंबर (ABHA, UHID)"),
        location = LOCATION_DOCS,
        contact = f("Treating doctor / family contact", "इलाज करने वाले डॉक्टर / पारिवारिक संपर्क"),
        phone = f("Contact phone", "संपर्क फ़ोन"),
        notes = f("Condition / medicines summary", "स्थिति / दवाओं का सारांश"),
        nomineeRelevant = false,
        guidanceEn = "For a long-lasting condition, keep the latest reports and prescriptions together so any doctor — or the family in an emergency — can see the full picture quickly.",
        guidanceHi = "लंबे समय की स्थिति के लिए ताज़ा रिपोर्ट और पर्चे एक साथ रखें ताकि कोई भी डॉक्टर — या आपातकाल में परिवार — पूरी तस्वीर जल्दी देख सके।"
    )

    private val BY_ORDER: Map<Int, EntryFields> = mapOf(
        10 to EntryFields(
            primary = null,
            ref = f("Full Aadhaar number (12 digits)", "पूरा आधार नंबर (12 अंक)", "1234 5678 9012", "1234 5678 9012"),
            location = LOCATION_DOCS,
            contact = null,
            phone = REGISTERED_MOBILE,
            notes = NOTES, nomineeRelevant = false,
            guidanceEn = "This is your family's own sealed copy, so write the full 12-digit Aadhaar number — banks and government offices ask for it, and the last 4 digits alone cannot be used on any form or login. Keep the printed dossier sealed and locked. Never write an OTP or PIN.",
            guidanceHi = "यह आपके परिवार की अपनी मुहरबंद प्रति है, इसलिए पूरा 12 अंकों का आधार नंबर लिखें — बैंक और सरकारी दफ़्तर यही माँगते हैं, और केवल अंतिम 4 अंकों से कोई फ़ॉर्म या लॉगिन नहीं होता। छपी प्रति मुहरबंद और ताले में रखें। OTP या PIN कभी न लिखें।"
        ),
        20 to EntryFields(
            primary = null,
            ref = f("Full PAN number", "पूरा PAN नंबर", "ABCDE1234F", "ABCDE1234F"),
            location = LOCATION_DOCS,
            contact = null,
            phone = REGISTERED_MOBILE,
            notes = NOTES, nomineeRelevant = false,
            guidanceEn = "Write the full PAN — it is the username for the Income Tax portal and is asked on bank and Demat forms; the last 4 characters alone cannot be used anywhere. Keep this dossier sealed, and never write any password or PIN.",
            guidanceHi = "पूरा PAN लिखें — यह आयकर पोर्टल का उपयोगकर्ता नाम है और बैंक व डीमैट फ़ॉर्म पर माँगा जाता है; केवल अंतिम 4 अक्षरों से कहीं काम नहीं होता। यह दस्तावेज़ मुहरबंद रखें, और कोई पासवर्ड या PIN कभी न लिखें।"
        ),
        30 to EntryFields(
            primary = null,
            ref = f("Passport / Voter ID no. (optional)", "पासपोर्ट / मतदाता पहचान संख्या (वैकल्पिक)"),
            location = LOCATION_DOCS,
            contact = null, phone = null,
            notes = NOTES, nomineeRelevant = false,
            guidanceEn = "Note where originals and photocopies live. Numbers are optional — location matters more in an emergency.",
            guidanceHi = "मूल और प्रतियाँ कहाँ हैं, यह लिखें। नंबर वैकल्पिक हैं — आपातकाल में स्थान अधिक मायने रखता है।"
        ),
        31 to EntryFields(
            primary = f("Name as on passport", "पासपोर्ट पर लिखा नाम"),
            ref = f("Passport number", "पासपोर्ट नंबर"),
            location = LOCATION_DOCS,
            contact = null, phone = null,
            notes = f("Issue date & renewal notes", "जारी तारीख और नवीनीकरण नोट"),
            nomineeRelevant = false,
            guidanceEn = "Note the expiry date — passports are renewed before they expire, and the old booklet holds your visas.",
            guidanceHi = "समाप्ति तारीख लिखें — पासपोर्ट समाप्ति से पहले नवीनीकृत होता है, और पुरानी पुस्तिका में आपके वीज़ा होते हैं।",
            maturity = f("Expiry date (valid till)", "समाप्ति तारीख (कब तक वैध)")
        ),
        32 to EntryFields(
            primary = f("Constituency / part no. (optional)", "निर्वाचन क्षेत्र / भाग सं. (वैकल्पिक)"),
            ref = f("Voter ID (EPIC) number", "मतदाता पहचान (EPIC) नंबर"),
            location = LOCATION_DOCS,
            contact = null, phone = null,
            notes = NOTES, nomineeRelevant = false,
            guidanceEn = "The EPIC number is asked on many forms; the original card is proof of address and identity.",
            guidanceHi = "EPIC नंबर कई फ़ॉर्मों पर माँगा जाता है; मूल कार्ड पता और पहचान का प्रमाण है।"
        ),
        33 to EntryFields(
            primary = f("Name as on licence", "लाइसेंस पर लिखा नाम"),
            ref = f("Driving licence number", "ड्राइविंग लाइसेंस नंबर"),
            location = LOCATION_DOCS,
            contact = null, phone = null,
            notes = f("Vehicle class & renewal notes", "वाहन श्रेणी और नवीनीकरण नोट"),
            nomineeRelevant = false,
            guidanceEn = "A licence is renewed around its expiry date; note which vehicle classes it covers.",
            guidanceHi = "लाइसेंस समाप्ति तारीख के आसपास नवीनीकृत होता है; कौन-सी वाहन श्रेणियाँ शामिल हैं लिखें।",
            maturity = f("Valid till (expiry date)", "कब तक वैध (समाप्ति तारीख)")
        ),
        40 to EntryFields(
            primary = null,
            ref = f("Will date / where registered (optional)", "वसीयत की तारीख / कहाँ पंजीकृत (वैकल्पिक)"),
            location = f("Where is the will kept?", "वसीयत कहाँ रखी है?"),
            contact = f("Executor name", "निष्पादक का नाम"),
            phone = f("Executor phone", "निष्पादक का फ़ोन"),
            notes = f("Alternate executor & notes", "वैकल्पिक निष्पादक और नोट"), nomineeRelevant = false,
            guidanceEn = "What is an executor? The executor is the trusted person you name in your will to carry out your wishes after you: they collect your assets, pay off loans and taxes, and distribute everything exactly as the will says, usually with your advocate's help for probate. Pick someone trustworthy, organised and likely to be available — and name an alternate executor in the notes in case the first person cannot act.",
            guidanceHi = "निष्पादक कौन होता है? निष्पादक वह भरोसेमंद व्यक्ति है जिसे आप अपनी वसीयत में नामित करते हैं ताकि आपके बाद आपकी इच्छाएँ पूरी हों: वे आपकी संपत्ति इकट्ठा करते हैं, ऋण और कर चुकाते हैं, और वसीयत के अनुसार सब वितरित करते हैं — आमतौर पर वकील की मदद से। कोई भरोसेमंद, व्यवस्थित और उपलब्ध व्यक्ति चुनें — और नोट में एक वैकल्पिक निष्पादक भी लिखें, यदि पहला व्यक्ति काम न कर सके।"
        ),
        50 to EntryFields(
            primary = f("Bank & branch", "बैंक और शाखा", "SBI — MG Road branch", "SBI — MG रोड शाखा"),
            ref = f("Savings account number (full)", "बचत खाता नंबर (पूरा)"),
            location = f("Passbook / cheque book location", "पासबुक / चेक बुक का स्थान"),
            contact = NOMINEE_NAME, phone = NOMINEE_PHONE,
            notes = NOTES, nomineeRelevant = true,
            guidanceEn = "SEBI/RBI nomination needs the nominee's name and relationship. Never write net-banking passwords or UPI PINs.",
            guidanceHi = "SEBI/RBI नॉमिनेशन के लिए नॉमिनी का नाम और रिश्ता चाहिए। नेट-बैंकिंग पासवर्ड या UPI PIN कभी न लिखें।"
        ),
        51 to EntryFields(
            primary = f("Bank & branch", "बैंक और शाखा"),
            ref = f("Current account number (full)", "चालू खाता नंबर (पूरा)"),
            location = f("Passbook / cheque book location", "पासबुक / चेक बुक का स्थान"),
            contact = NOMINEE_NAME, phone = NOMINEE_PHONE,
            notes = NOTES, nomineeRelevant = true,
            guidanceEn = "Current accounts serve business / HUF activity. Never write net-banking passwords or UPI PINs.",
            guidanceHi = "चालू खाते व्यवसाय / HUF के लिए होते हैं। नेट-बैंकिंग पासवर्ड या UPI PIN कभी न लिखें।"
        ),
        60 to EntryFields(
            primary = f("Bank / post office", "बैंक / डाकघर"),
            ref = f("FD receipt / account number", "FD रसीद / खाता नंबर"),
            location = f("Where are the receipts kept?", "रसीदें कहाँ रखी हैं?"),
            contact = NOMINEE_NAME, phone = NOMINEE_PHONE,
            notes = NOTES, nomineeRelevant = true,
            guidanceEn = "Note the maturity date — on maturity the bank needs the nominee or account holder to claim or renew. A registered nominee makes the claim far simpler.",
            guidanceHi = "परिपक्वता तारीख लिखें — परिपक्वता पर बैंक को दावा या नवीनीकरण के लिए नॉमिनी या खाताधारक चाहिए। दर्ज नॉमिनी से दावा बहुत आसान हो जाता है।",
            maturity = f("Maturity date", "परिपक्वता तारीख")
        ),
        61 to EntryFields(
            primary = f("Bank / post office", "बैंक / डाकघर"),
            ref = f("RD account number", "RD खाता नंबर"),
            location = f("Where is the receipt kept?", "रसीद कहाँ रखी है?"),
            contact = NOMINEE_NAME, phone = NOMINEE_PHONE,
            notes = f("Monthly instalment & notes", "मासिक किस्त और नोट"),
            nomineeRelevant = true,
            guidanceEn = "Note the monthly instalment and maturity date so no instalment is missed.",
            guidanceHi = "मासिक किस्त और परिपक्वता तारीख लिखें ताकि कोई किस्त न छूटे।",
            maturity = f("Maturity date", "परिपक्वता तारीख")
        ),
        70 to EntryFields(
            primary = f("Bank & branch", "बैंक और शाखा"),
            ref = f("Locker number", "लॉकर नंबर"),
            location = f("Where is the locker key kept?", "लॉकर की चाबी कहाँ रखी है?"),
            contact = f("Joint holder name", "संयुक्त धारक का नाम"),
            phone = f("Joint holder phone", "संयुक्त धारक का फ़ोन"),
            notes = NOTES, nomineeRelevant = true,
            guidanceEn = "Banks ask for the locker nominee/joint holder first. The key's location is what the family will hunt for.",
            guidanceHi = "बैंक सबसे पहले लॉकर नॉमिनी/संयुक्त धारक पूछता है। चाबी का स्थान ही परिवार खोजेगा।"
        ),
        80 to EntryFields(
            primary = f("Issuing bank", "जारी करने वाला बैंक"),
            ref = f("Card last 4 digits", "कार्ड के अंतिम 4 अंक", "···· 1234", "···· 1234"),
            location = f("Where are the cards kept?", "कार्ड कहाँ रखे हैं?"),
            contact = null, phone = null,
            notes = NOTES, nomineeRelevant = false,
            guidanceEn = "Only the last 4 digits identify a card. Never write the CVV, ATM PIN, OTP, or full card number.",
            guidanceHi = "केवल अंतिम 4 अंक कार्ड की पहचान देते हैं। CVV, ATM PIN, OTP या पूरा कार्ड नंबर कभी न लिखें।"
        ),
        90 to EntryFields(
            primary = f("Broker / DP", "ब्रोकर / DP", "Zerodha / Groww", "Zerodha / Groww"),
            ref = f("DP ID / Client ID", "DP ID / क्लाइंट ID"),
            location = f("Statements / documents location", "स्टेटमेंट / दस्तावेज़ों का स्थान"),
            contact = NOMINEE_NAME, phone = NOMINEE_PHONE,
            notes = NOTES, nomineeRelevant = true,
            guidanceEn = "SEBI's nomination framework asks for nominee name + relationship (share % optional). Never write broker passwords.",
            guidanceHi = "SEBI नॉमिनेशन ढाँचे में नॉमिनी का नाम + रिश्ता चाहिए (हिस्सा % वैकल्पिक)। ब्रोकर पासवर्ड कभी न लिखें।"
        ),
        91 to EntryFields(
            primary = f("AMC / platform", "AMC / प्लेटफ़ॉर्म", "CAMS / KFintech / Zerodha", "CAMS / KFintech / Zerodha"),
            ref = f("Folio number", "फोलियो नंबर"),
            location = f("Statements location", "स्टेटमेंट का स्थान"),
            contact = NOMINEE_NAME, phone = NOMINEE_PHONE,
            notes = f("Schemes & notes", "योजनाएँ और नोट"),
            nomineeRelevant = true,
            guidanceEn = "One record per AMC folio — add more with +. ELSS and close-ended funds have a maturity / lock-in date; open-ended funds have none.",
            guidanceHi = "हर AMC फोलियो का एक रिकॉर्ड — + से और जोड़ें। ELSS और क्लोज़-एंडेड फंड की परिपक्वता / लॉक-इन तारीख होती है; ओपन-एंडेड की नहीं।",
            maturity = f("Maturity date (if any)", "परिपक्वता तारीख (यदि कोई हो)")
        ),
        100 to EntryFields(
            primary = f("Employer / EPFO", "नियोक्ता / EPFO"),
            ref = f("UAN (Universal Account Number)", "UAN (यूनिवर्सल खाता नंबर)"),
            location = LOCATION_DOCS,
            contact = NOMINEE_NAME, phone = NOMINEE_PHONE,
            notes = NOTES, nomineeRelevant = true,
            guidanceEn = "EPFO e-nomination covers PF, pension (EPS) and EDLI insurance. Nominees are family members, shares must total 100%, and a minor nominee needs a guardian.",
            guidanceHi = "EPFO ई-नॉमिनेशन PF, पेंशन (EPS) और EDLI बीमा को कवर करता है। नॉमिनी परिवार के सदस्य होते हैं, हिस्सों का योग 100% होना चाहिए, और नाबालिग नॉमिनी के लिए संरक्षक चाहिए।",
            maturity = f("Retirement / maturity date (if known)", "सेवानिवृत्ति / परिपक्वता तारीख (यदि ज्ञात हो)")
        ),
        110 to EntryFields(
            primary = f("Bank / post office branch", "बैंक / डाकघर शाखा"),
            ref = f("PPF account number", "PPF खाता नंबर"),
            location = f("Passbook location", "पासबुक का स्थान"),
            contact = NOMINEE_NAME, phone = NOMINEE_PHONE,
            notes = NOTES, nomineeRelevant = true,
            guidanceEn = "A PPF nominee can be changed at the branch/post office. Nominee name + relationship is what a claim needs first.",
            guidanceHi = "PPF नॉमिनी शाखा/डाकघर में बदला जा सकता है। दावे के लिए सबसे पहले नॉमिनी का नाम + रिश्ता चाहिए।",
            maturity = f("Maturity date (15 years from opening)", "परिपक्वता तारीख (खोलने से 15 वर्ष)")
        ),
        120 to EntryFields(
            primary = f("POP / employer NPS", "POP / नियोक्ता NPS"),
            ref = f("PRAN", "PRAN"),
            location = LOCATION_DOCS,
            contact = NOMINEE_NAME, phone = NOMINEE_PHONE,
            notes = NOTES, nomineeRelevant = true,
            guidanceEn = "Keep nominee and family-pension details current in NPS — they decide who receives the corpus.",
            guidanceHi = "NPS में नॉमिनी और पारिवारिक पेंशन विवरण अद्यतन रखें — यही तय करता है कि राशि किसे मिलेगी।"
        ),
        130 to EntryFields(
            primary = f("Bank / jeweller", "बैंक / जौहरी"),
            ref = f("SGB certificate / receipt no.", "SGB प्रमाणपत्र / रसीद नंबर"),
            location = f("Where is the gold stored?", "सोना कहाँ रखा है?"),
            contact = NOMINEE_NAME, phone = NOMINEE_PHONE,
            notes = NOTES, nomineeRelevant = true,
            guidanceEn = "Sovereign Gold Bonds can carry a nominee, just like a bank deposit. Receipts and locker slips go in the notes.",
            guidanceHi = "सॉवरेन गोल्ड बॉन्ड में बैंक जमा की तरह नॉमिनी हो सकता है। रसीदें और लॉकर पर्चियाँ नोट में लिखें।"
        ),
        140 to EntryFields(
            primary = f("Insurer", "बीमाकर्ता", "LIC of India", "भारतीय जीवन बीमा निगम"),
            ref = f("Policy number", "पॉलिसी नंबर"),
            location = f("Where is the policy bond kept?", "पॉलिसी बॉन्ड कहाँ रखा है?"),
            contact = NOMINEE_NAME,
            phone = f("Agent phone", "एजेंट का फ़ोन"),
            notes = f("Sum assured, premium & notes", "बीमित राशि, प्रीमियम और नोट"),
            nomineeRelevant = true,
            guidanceEn = "IRDAI expects nominee name, relationship and contact details on the policy (an appointee if the nominee is a minor). The agent who sold it is usually fastest to start a claim.",
            guidanceHi = "IRDAI के अनुसार पॉलिसी पर नॉमिनी का नाम, रिश्ता और संपर्क विवरण होना चाहिए (नाबालिग नॉमिनी के लिए नियुक्त व्यक्ति)। जिस एजेंट ने पॉलिसी दी, वही दावा सबसे तेज़ शुरू करता है।"
        ),
        150 to EntryFields(
            primary = f("Insurer / TPA", "बीमाकर्ता / TPA"),
            ref = f("Policy number", "पॉलिसी नंबर"),
            location = f("Where are the health cards kept?", "स्वास्थ्य कार्ड कहाँ रखे हैं?"),
            contact = NOMINEE_NAME,
            phone = f("Agent / TPA helpline", "एजेंट / TPA हेल्पलाइन"),
            notes = f("Sum insured, cashless hospitals & notes", "बीमित राशि, कैशलेस अस्पताल और नोट"),
            nomineeRelevant = true,
            guidanceEn = "In a hospital emergency the family needs the policy number and TPA helpline first — keep cashless hospital notes here.",
            guidanceHi = "अस्पताल की आपात स्थिति में परिवार को सबसे पहले पॉलिसी नंबर और TPA हेल्पलाइन चाहिए — कैशलेस अस्पताल नोट यहाँ रखें।"
        ),
        160 to EntryFields(
            primary = f("Insurer", "बीमाकर्ता"),
            ref = f("Vehicle registration (RC) no.", "वाहन पंजीकरण (RC) नंबर"),
            location = f("Where are the RC & papers kept?", "RC और काग़ज़ कहाँ रखे हैं?"),
            contact = null,
            phone = f("Insurer helpline", "बीमाकर्ता हेल्पलाइन"),
            notes = NOTES, nomineeRelevant = false,
            guidanceEn = "Note where the spare key is in the notes. RC location is what matters after an accident.",
            guidanceHi = "नोट में अतिरिक्त चाबी का स्थान लिखें। दुर्घटना के बाद RC का स्थान सबसे मायने रखता है।"
        ),
        170 to EntryFields(
            primary = f("Sub-registrar / authority", "उप-पंजीयक / प्राधिकरण"),
            ref = f("Registry / khata number", "रजिस्ट्री / खाता नंबर"),
            location = f("Where are the original deeds kept?", "मूल काग़ज़ कहाँ रखे हैं?"),
            contact = f("Advocate / family contact", "वकील / पारिवारिक संपर्क"),
            phone = f("Contact phone", "संपर्क फ़ोन"),
            notes = NOTES, nomineeRelevant = false,
            guidanceEn = "Originals, encumbrance certificate and tax receipts are usually asked together — note if they are in a locker.",
            guidanceHi = "मूल काग़ज़, भार-मुक्ति प्रमाणपत्र और कर रसीदें आमतौर पर साथ माँगी जाती हैं — लॉकर में हों तो लिखें।"
        ),
        180 to EntryFields(
            primary = f("Lender (bank / NBFC)", "ऋणदाता (बैंक / NBFC)"),
            ref = f("Loan account number", "ऋण खाता नंबर"),
            location = f("Where are the loan papers kept?", "ऋण के काग़ज़ कहाँ रखे हैं?"),
            contact = f("Co-borrower / nominee name", "सह-ऋणी / नॉमिनी का नाम"),
            phone = NOMINEE_PHONE,
            notes = NOTES, nomineeRelevant = true,
            guidanceEn = "Note whether a loan-linked credit-shield life cover exists — families often miss claiming it.",
            guidanceHi = "लिखें कि ऋण से जुड़ा क्रेडिट-शील्ड जीवन बीमा है या नहीं — परिवार अक्सर इसका दावा करना भूल जाता है।"
        ),
        190 to EntryFields(
            primary = null, ref = null,
            location = f("Where is the unlock note kept?", "अनलॉक नोट कहाँ रखा है?"),
            contact = f("Trusted person", "भरोसेमंद व्यक्ति"),
            phone = f("Their phone", "उनका फ़ोन"),
            notes = NOTES, nomineeRelevant = false,
            guidanceEn = "Never type the phone PIN itself here. Write where the sealed note lives and who may open it.",
            guidanceHi = "फ़ोन का PIN स्वयं यहाँ कभी न लिखें। मुहरबंद नोट कहाँ है और कौन खोल सकता है, यह लिखें।"
        ),
        200 to EntryFields(
            primary = f("Email provider", "ईमेल प्रदाता", "Gmail / Apple", "Gmail / Apple"),
            ref = f("Recovery email ID", "रिकवरी ईमेल ID"),
            location = PASSWORD_KEPT_AT,
            contact = f("Recovery contact name", "रिकवरी संपर्क का नाम"),
            phone = REGISTERED_MOBILE,
            notes = NOTES, nomineeRelevant = false,
            guidanceEn = "This address resets almost everything else. Record the account it belongs to and where its password is kept — never the password.",
            guidanceHi = "यह पता लगभग हर चीज़ रीसेट करता है। यह किस खाते का है और पासवर्ड कहाँ रखा है, लिखें — पासवर्ड कभी नहीं।"
        ),
        210 to EntryFields(
            primary = null,
            ref = f("Registered mobile / username", "पंजीकृत मोबाइल / उपयोगकर्ता नाम"),
            location = f("Where is the security note kept?", "सुरक्षा नोट कहाँ रखा है?"),
            contact = null, phone = REGISTERED_MOBILE,
            notes = NOTES, nomineeRelevant = false,
            guidanceEn = "DigiLocker holds verified Aadhaar/PAN copies the family may need immediately. Note the note's location, not the PIN.",
            guidanceHi = "DigiLocker में सत्यापित आधार/PAN प्रतियाँ होती हैं जो परिवार को तुरंत चाहिए हो सकती हैं। नोट का स्थान लिखें, PIN नहीं।"
        ),
        220 to EntryFields(
            primary = f("CA firm", "CA फ़र्म"),
            ref = null,
            location = f("Where are ITR / Form 16 files kept?", "ITR / फ़ॉर्म 16 फ़ाइलें कहाँ रखी हैं?"),
            contact = f("CA name", "CA का नाम"), phone = f("CA phone", "CA का फ़ोन"),
            notes = NOTES, nomineeRelevant = false,
            guidanceEn = "After a death, the CA files the final ITR and finds every investment proof. Their number saves weeks.",
            guidanceHi = "मृत्यु के बाद CA अंतिम ITR दाखिल करता है और हर निवेश प्रमाण खोजता है। उनका नंबर हफ़्ते बचाता है।"
        ),
        230 to EntryFields(
            primary = f("Law firm / chamber", "कानून फ़र्म / चैंबर"),
            ref = null,
            location = f("Where are will / property papers kept?", "वसीयत / संपत्ति के काग़ज़ कहाँ रखे हैं?"),
            contact = f("Advocate name", "वकील का नाम"), phone = f("Advocate phone", "वकील का फ़ोन"),
            notes = NOTES, nomineeRelevant = false,
            guidanceEn = "The advocate who drafted the will or property papers is the first call for probate questions.",
            guidanceHi = "जिस वकील ने वसीयत या संपत्ति के काग़ज़ बनाए, प्रोबेट सवालों के लिए पहला फ़ोन उन्हीं को जाता है।"
        ),
        240 to EntryFields(
            primary = f("Insurer / agency", "बीमाकर्ता / एजेंसी"),
            ref = null,
            location = f("Where are the policy files kept?", "पॉलिसी फ़ाइलें कहाँ रखी हैं?"),
            contact = f("Agent name", "एजेंट का नाम"), phone = f("Agent phone", "एजेंट का फ़ोन"),
            notes = NOTES, nomineeRelevant = false,
            guidanceEn = "A good agent files life/health claims on the family's behalf — keep their mobile here.",
            guidanceHi = "अच्छा एजेंट परिवार की ओर से जीवन/स्वास्थ्य दावे दाखिल करता है — उनका मोबाइल यहाँ रखें।"
        ),
        250 to EntryFields(
            primary = f("Google account email ID", "Google खाता ईमेल ID", "name@gmail.com", "name@gmail.com"),
            ref = f("Recovery email ID", "रिकवरी ईमेल ID"),
            location = PASSWORD_KEPT_AT,
            contact = f("Inactive Account Manager trusted contact", "Inactive Account Manager भरोसेमंद संपर्क"),
            phone = REGISTERED_MOBILE,
            notes = NOTES, nomineeRelevant = false,
            guidanceEn = "Turn on Google's Inactive Account Manager (myaccount.google.com/inactive): pick 3–18 months of inactivity and up to 10 trusted contacts, verified by phone. Store where the password is kept — never the password.",
            guidanceHi = "Google का Inactive Account Manager चालू करें (myaccount.google.com/inactive): 3–18 महीने की निष्क्रियता और अधिकतम 10 भरोसेमंद संपर्क चुनें, फ़ोन से सत्यापित। पासवर्ड कहाँ रखा है लिखें — पासवर्ड कभी नहीं।"
        ),
        260 to EntryFields(
            primary = f("Apple ID email", "Apple ID ईमेल"),
            ref = f("Recovery email ID", "रिकवरी ईमेल ID"),
            location = PASSWORD_KEPT_AT,
            contact = f("Legacy Contact name", "Legacy Contact का नाम"),
            phone = REGISTERED_MOBILE,
            notes = f("Where is the Legacy Contact access key kept?", "Legacy Contact एक्सेस कुंजी कहाँ रखी है?"),
            nomineeRelevant = false,
            guidanceEn = "Apple Legacy Contact: your contact needs the generated access key plus a death certificate; iCloud Keychain passwords are never shared. Print the key and file it with the will.",
            guidanceHi = "Apple Legacy Contact: संपर्क को बनाई गई एक्सेस कुंजी और मृत्यु प्रमाणपत्र चाहिए; iCloud Keychain पासवर्ड कभी साझा नहीं होते। कुंजी छापकर वसीयत के साथ रखें।"
        ),
        270 to EntryFields(
            primary = f("Email ID & provider", "ईमेल ID और प्रदाता", "Outlook / Yahoo / Rediff", "Outlook / Yahoo / Rediff"),
            ref = f("Recovery email ID", "रिकवरी ईमेल ID"),
            location = PASSWORD_KEPT_AT,
            contact = f("Family contact for this account", "इस खाते के लिए पारिवारिक संपर्क"),
            phone = REGISTERED_MOBILE,
            notes = NOTES, nomineeRelevant = false,
            guidanceEn = "Old email IDs often guard bank and tax accounts. List each one with its recovery path — never its password.",
            guidanceHi = "पुरानी ईमेल IDs अक्सर बैंक और कर खातों की रक्षा करती हैं। हर एक को उसकी रिकवरी राह के साथ लिखें — पासवर्ड कभी नहीं।"
        ),
        280 to EntryFields(
            primary = f("Portal username (usually PAN)", "पोर्टल उपयोगकर्ता नाम (आमतौर पर PAN)"),
            ref = f("Registered email ID", "पंजीकृत ईमेल ID"),
            location = PASSWORD_KEPT_AT,
            contact = f("CA / family contact", "CA / पारिवारिक संपर्क"),
            phone = REGISTERED_MOBILE,
            notes = NOTES, nomineeRelevant = false,
            guidanceEn = "The Income Tax account is needed to file the final return. Username + registered mobile/email is enough for the family to recover access lawfully.",
            guidanceHi = "अंतिम रिटर्न दाखिल करने के लिए आयकर खाता चाहिए। परिवार को कानूनी रूप से पहुँच वापस पाने के लिए उपयोगकर्ता नाम + पंजीकृत मोबाइल/ईमेल काफ़ी है।"
        ),
        290 to EntryFields(
            primary = f("Password manager", "पासवर्ड मैनेजर", "Bitwarden / 1Password / KeePass", "Bitwarden / 1Password / KeePass"),
            ref = f("Vault email ID", "वॉल्ट ईमेल ID"),
            location = f("Master-password hint / sealed copy kept at", "मास्टर-पासवर्ड संकेत / मुहरबंद प्रति कहाँ रखी है"),
            contact = f("Emergency access contact", "आपातकालीन पहुँच संपर्क"),
            phone = REGISTERED_MOBILE,
            notes = NOTES, nomineeRelevant = false,
            guidanceEn = "This is the safe way to hand over logins: keep every password inside the manager, enable its emergency-access feature for a trusted person, and record only where the master-password hint is sealed.",
            guidanceHi = "लॉगिन सौंपने का सुरक्षित तरीका यही है: हर पासवर्ड मैनेजर में रखें, भरोसेमंद व्यक्ति के लिए उसका आपातकालीन-पहुँच फ़ीचर चालू करें, और केवल मास्टर-पासवर्ड संकेत कहाँ मुहरबंद है यह लिखें।"
        ),
        300 to EntryFields(
            primary = f("Portal name", "पोर्टल का नाम", "EPFO / bank / Demat portal", "EPFO / बैंक / डीमैट पोर्टल"),
            ref = f("Username / client ID", "उपयोगकर्ता नाम / क्लाइंट ID"),
            location = PASSWORD_KEPT_AT,
            contact = f("Family contact", "पारिवारिक संपर्क"),
            phone = REGISTERED_MOBILE,
            notes = NOTES, nomineeRelevant = false,
            guidanceEn = "For official portals, username + registered mobile/email lets family use the official forgot-password and claim routes. Passwords themselves stay out.",
            guidanceHi = "आधिकारिक पोर्टलों के लिए उपयोगकर्ता नाम + पंजीकृत मोबाइल/ईमेल से परिवार आधिकारिक पासवर्ड-भूलें और दावा मार्ग उपयोग कर सकता है। पासवर्ड बाहर ही रहते हैं।"
        ),
        310 to EntryFields(
            primary = f("Platform & username", "प्लेटफ़ॉर्म और उपयोगकर्ता नाम", "WhatsApp / Facebook / Instagram", "WhatsApp / Facebook / Instagram"),
            ref = f("Registered email ID", "पंजीकृत ईमेल ID"),
            location = PASSWORD_KEPT_AT,
            contact = f("Legacy / memorial contact", "विरासत / स्मारक संपर्क"),
            phone = REGISTERED_MOBILE,
            notes = NOTES, nomineeRelevant = false,
            guidanceEn = "Facebook offers a Legacy Contact for a memorialised profile; other platforms close accounts on proof of death. Note which applies — never the password.",
            guidanceHi = "Facebook स्मारक प्रोफ़ाइल के लिए Legacy Contact देता है; अन्य प्लेटफ़ॉर्म मृत्यु के प्रमाण पर खाते बंद करते हैं। कौन-सा लागू है लिखें — पासवर्ड कभी नहीं।"
        ),
        320 to EDUCATION_FIELDS, 330 to EDUCATION_FIELDS, 340 to EDUCATION_FIELDS,
        350 to EDUCATION_FIELDS, 360 to EDUCATION_FIELDS, 370 to EDUCATION_FIELDS,
        380 to EDUCATION_FIELDS, 390 to EDUCATION_FIELDS, 400 to EDUCATION_FIELDS,
        410 to MEDICAL_FIELDS, 420 to MEDICAL_FIELDS, 430 to MEDICAL_FIELDS,
        440 to MEDICAL_FIELDS, 450 to MEDICAL_FIELDS, 460 to MEDICAL_FIELDS,
        470 to MEDICAL_FIELDS
    )

    fun forItem(item: ChecklistItem): EntryFields {
        if (item.specOrder != 0) BY_ORDER[item.specOrder]?.let { return it }
        if (!item.isUserCreated) BY_ORDER[item.sortOrder]?.let { return it }
        return generic(item.categoryEnum)
    }

    /** Sensible defaults for user-created records, by category. */
    fun generic(category: AssetCategory): EntryFields = when (category) {
        AssetCategory.BANKING_FUNDS, AssetCategory.INVESTMENTS, AssetCategory.INSURANCE -> EntryFields(
            primary = f("Institution / branch", "संस्था / शाखा"),
            ref = f("Account / policy number", "खाता / पॉलिसी नंबर"),
            location = LOCATION_DOCS,
            contact = NOMINEE_NAME, phone = NOMINEE_PHONE,
            notes = NOTES, nomineeRelevant = true,
            guidanceEn = "Record locations and identifiers only — never passwords, PINs, OTPs or CVVs.",
            guidanceHi = "केवल स्थान और पहचान दर्ज करें — पासवर्ड, PIN, OTP या CVV कभी नहीं।"
        )
        AssetCategory.DIGITAL_LEGACY -> EntryFields(
            primary = f("Account / email ID", "खाता / ईमेल ID"),
            ref = f("Username / recovery email", "उपयोगकर्ता नाम / रिकवरी ईमेल"),
            location = PASSWORD_KEPT_AT,
            contact = f("Trusted / legacy contact", "भरोसेमंद / विरासत संपर्क"),
            phone = REGISTERED_MOBILE,
            notes = NOTES, nomineeRelevant = false,
            guidanceEn = "Store the map, not the keys: account ID, registered mobile, recovery email and where the password is kept.",
            guidanceHi = "चाबियाँ नहीं, नक्शा रखें: खाता ID, पंजीकृत मोबाइल, रिकवरी ईमेल और पासवर्ड कहाँ रखा है।"
        )
        else -> EntryFields(
            primary = f("Institution / branch", "संस्था / शाखा"),
            ref = f("Reference (optional)", "संदर्भ (वैकल्पिक)"),
            location = LOCATION_DOCS,
            contact = f("Contact person", "संपर्क व्यक्ति"),
            phone = f("Contact phone", "संपर्क फ़ोन"),
            notes = NOTES, nomineeRelevant = false,
            guidanceEn = "Record locations and metadata only — never passwords, PINs, OTPs or CVVs.",
            guidanceHi = "केवल स्थान और जानकारी दर्ज करें — पासवर्ड, PIN, OTP या CVV कभी नहीं।"
        )
    }
}
