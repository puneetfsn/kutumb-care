package com.puneetfsn.kutumbcare.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.puneetfsn.kutumbcare.data.AppLanguage
import com.puneetfsn.kutumbcare.data.AssetCategory
import com.puneetfsn.kutumbcare.data.NomineeStatus

/**
 * Every user-facing string in English and Hindi, selected by the language the
 * family picked on first launch. Kept as plain Kotlin (not XML resources) so
 * the choice can switch instantly without recreating the activity, and so the
 * same wording can be unit tested.
 */
class UiStrings(val hindi: Boolean) {

    private fun pick(en: String, hi: String) = if (hindi) hi else en

    val tagline get() = pick("A private emergency dossier that never leaves this phone.", "एक निजी आपातकालीन दस्तावेज़ जो इस फ़ोन से बाहर कभी नहीं जाता।")
    val home get() = pick("Home", "मुख्य")
    val recordsNav get() = pick("Records", "रिकॉर्ड")
    val settingsNav get() = pick("Settings", "सेटिंग")
    val prepared get() = pick("prepared", "तैयार")
    val recorded get() = pick("Recorded", "दर्ज किए गए")
    val nomineeAlerts get() = pick("Nominee alerts", "नॉमिनी अलर्ट")
    val generateDossier get() = pick("Generate emergency dossier", "आपातकालीन दस्तावेज़ बनाएँ")
    val preparing get() = pick("Preparing dossier…", "दस्तावेज़ तैयार हो रहा है…")
    val categories get() = pick("Categories", "श्रेणियाँ")
    val familyProfileLink get() = pick("Family profile", "पारिवारिक प्रोफ़ाइल")
    val checklistPreparing get() = pick("Your checklist is still being prepared.", "आपकी सूची अभी तैयार हो रही है।")
    val noMatch get() = pick("No records match this filter yet. Add one with the + button.", "इस फ़िल्टर से अभी कोई रिकॉर्ड मेल नहीं खाता। + बटन से एक जोड़ें।")
    val searchPlaceholder get() = pick("Search bank, UAN, locker, agent…", "बैंक, UAN, लॉकर, एजेंट खोजें…")
    val all get() = pick("All", "सभी")
    val familyRecords get() = pick("Family records", "पारिवारिक रिकॉर्ड")
    val addRecord get() = pick("Add record", "रिकॉर्ड जोड़ें")
    val markRecorded get() = pick("Mark recorded", "दर्ज किया गया चिह्नित करें")
    val markIncomplete get() = pick("Mark incomplete", "अधूरा चिह्नित करें")
    val loading get() = pick("Loading…", "लोड हो रहा है…")
    val back get() = pick("Back", "वापस")
    val delete get() = pick("Delete", "हटाएँ")
    val newRecord get() = pick("New record", "नया रिकॉर्ड")
    val record get() = pick("Record", "रिकॉर्ड")
    val title get() = pick("Title", "शीर्षक")
    val institution get() = pick("Institution / branch", "संस्था / शाखा")
    val institutionPlaceholder get() = pick("SBI — MG Road branch", "SBI — MG रोड शाखा")
    val refLabel get() = pick("Account / policy / UAN / folio (optional)", "खाता / पॉलिसी / UAN / फोलियो (वैकल्पिक)")
    val location get() = pick("Physical document location", "दस्तावेज़ रखने का स्थान")
    val locationPlaceholder get() = pick("Master bedroom almirah / blue folder", "मुख्य शयनकक्ष अलमारी / नीला फ़ोल्डर")
    val nomineeStatus get() = pick("Nominee status", "नॉमिनी की स्थिति")
    val contactPerson get() = pick("Contact person / agent", "संपर्क व्यक्ति / एजेंट")
    val contactPhone get() = pick("Contact phone", "संपर्क फ़ोन")
    val notes get() = pick("Notes for the family", "परिवार के लिए नोट")
    val saveRecord get() = pick("Save record", "रिकॉर्ड सहेजें")
    val privacyBanner get() = pick(
        "Record locations and metadata only. Never save net-banking passwords, UPI PINs, ATM PINs, OTPs or CVVs.",
        "केवल स्थान और जानकारी दर्ज करें। नेट-बैंकिंग पासवर्ड, UPI PIN, ATM PIN, OTP या CVV कभी न सहेजें।"
    )
    val nomineeAlertBody get() = pick(
        "Unclaimed deposits often sit with banks because no nominee was registered.",
        "बिना नॉमिनी के बैंक में पैसा अक्सर बिना दावे के पड़ा रहता है।"
    )
    fun nomineeAlertTitle(count: Int): String = if (hindi) {
        "$count खातों में नॉमिनी चाहिए"
    } else {
        "$count ${if (count == 1) "account needs" else "accounts need"} a nominee"
    }

    val settingsTitle get() = pick("Privacy & family", "गोपनीयता और परिवार")
    val noInternetNote get() = pick(
        "Kutumb Care has no internet permission, no account and no cloud. Clearing app data is the only way to recover a forgotten PIN.",
        "कुटुम्ब केयर के पास इंटरनेट अनुमति, खाता या क्लाउड नहीं है। भूला हुआ PIN वापस पाने का एकमात्र तरीका ऐप डेटा साफ़ करना है।"
    )
    val primaryPerson get() = pick("Primary person", "मुख्य व्यक्ति")
    val familyLabel get() = pick("Family label", "परिवार का नाम")
    val familyLabelPlaceholder get() = pick("Kumar family", "शर्मा परिवार")
    val emergencyName get() = pick("Emergency contact name", "आपातकालीन संपर्क का नाम")
    val emergencyPhone get() = pick("Emergency contact phone", "आपातकालीन संपर्क फ़ोन")
    val saveProfile get() = pick("Save profile", "प्रोफ़ाइल सहेजें")
    val appearance get() = pick("Appearance", "रूप-रंग")
    val themeSystem get() = pick("System", "सिस्टम")
    val themeLight get() = pick("Light", "हल्का")
    val themeDark get() = pick("Dark", "गहरा")
    val language get() = pick("Language", "भाषा")
    val languageNote get() = pick("Changes every screen, including the seeded records.", "इससे हर स्क्रीन और सहेजे गए रिकॉर्ड की भाषा बदल जाती है।")
    val english get() = "English"
    val hindiName get() = "हिन्दी"
    val pinSection get() = pick("4-digit app PIN", "4 अंकों का ऐप PIN")
    val pinOn get() = pick("PIN lock is on. The PIN is hashed on this device only.", "PIN लॉक चालू है। PIN केवल इस डिवाइस पर हैश करके रखा जाता है।")
    val pinOff get() = pick("Optional lock so a roommate cannot open the dossier.", "वैकल्पिक लॉक ताकि कोई और दस्तावेज़ न खोल सके।")
    val newPin get() = pick("New PIN", "नया PIN")
    val confirmPin get() = pick("Confirm PIN", "PIN की पुष्टि करें")
    val setPin get() = pick("Set PIN", "PIN सेट करें")
    val removePin get() = pick("Remove PIN", "PIN हटाएँ")
    val offlineBackup get() = pick("Offline backup", "ऑफ़लाइन बैकअप")
    val backupNote get() = pick(
        "Export a JSON file you can keep on a USB drive. The backup does not include the app PIN.",
        "JSON फ़ाइल निर्यात करें जिसे आप USB ड्राइव पर रख सकते हैं। बैकअप में ऐप PIN शामिल नहीं होता।"
    )
    val exportBackup get() = pick("Export JSON backup", "JSON बैकअप निर्यात करें")
    val restoreBackup get() = pick("Restore JSON backup", "JSON बैकअप बहाल करें")
    val about get() = pick("About", "परिचय")
    val aboutText get() = pick(
        "Kutumb Care is a 100% offline family emergency checklist for India. It helps your people find Aadhaar, bank nominees, LIC policies, EPF / UAN, property deeds and trusted contacts — without ever storing passwords.",
        "कुटुम्ब केयर भारत के लिए 100% ऑफ़लाइन पारिवारिक आपातकालीन सूची है। यह आपके लोगों को आधार, बैंक नॉमिनी, LIC पॉलिसी, EPF / UAN, संपत्ति के काग़ज़ात और भरोसेमंद संपर्क खोजने में मदद करता है — बिना कभी पासवर्ड रखे।"
    )
    val pinPrompt get() = pick("Enter your 4-digit PIN to open the family dossier.", "पारिवारिक दस्तावेज़ खोलने के लिए अपना 4 अंकों का PIN डालें।")
    val pinWrong get() = pick("That PIN does not match", "यह PIN मेल नहीं खाता")
    val chooseLanguageTitle get() = pick("Choose your language", "अपनी भाषा चुनें")
    val chooseLanguageSub get() = pick("You can change this later in Settings.", "आप इसे बाद में सेटिंग में बदल सकते हैं।")
    val customRecord get() = pick("Custom family record", "अपना पारिवारिक रिकॉर्ड")
    val discardTitle get() = pick("Discard changes?", "बदलाव छोड़ें?")
    val discardBody get() = pick("You have unsaved changes in this record.", "इस रिकॉर्ड में बिना सहेजे बदलाव हैं।")
    val discardKeep get() = pick("Keep editing", "संपादन जारी रखें")
    val discardLeave get() = pick("Discard", "छोड़ें")
    val pdfFinePrint get() = pick(
        "Fine print: This dossier lists where things are kept so family can find them. It never contains passwords, UPI/ATM PINs, OTPs or CVVs. Keep it sealed, share it only with trusted family, and review it every year.",
        "छोटा अक्षर: यह दस्तावेज़ बताता है कि चीज़ें कहाँ रखी हैं ताकि परिवार उन्हें खोज सके। इसमें पासवर्ड, UPI/ATM PIN, OTP या CVV कभी नहीं होते। इसे मुहरबंद रखें, केवल भरोसेमंद परिवार से साझा करें, और हर साल समीक्षा करें।"
    )

    // ---- PDF dossier ----
    val pdfTitle get() = pick("FAMILY EMERGENCY DOSSIER", "पारिवारिक आपातकालीन दस्तावेज़")
    val pdfPreparedFor get() = pick("Prepared for peaceful handover", "शांतिपूर्ण सौंपने के लिए तैयार")
    val pdfReady get() = pick("ready", "तैयार")
    val pdfRecordsCaptured get() = pick("records captured", "रिकॉर्ड दर्ज")
    val pdfEmergencyContact get() = pick("Emergency contact", "आपातकालीन संपर्क")
    val pdfHowTo get() = pick("How to use this binder", "इस बाइंडर का उपयोग कैसे करें")
    val pdfHowToLines: List<String>
        get() = if (hindi) listOf(
            "इस दस्तावेज़ को A4 काग़ज़ पर छापें। एक मुहरबंद प्रति किसी भरोसेमंद पारिवारिक सदस्य के पास और एक बैंक लॉकर में रखें।",
            "हर भाग में बताया गया है कि मूल काग़ज़ कहाँ हैं, संस्था कौन-सी है, खाता या पॉलिसी नंबर (वैकल्पिक), नॉमिनी की स्थिति और मदद करने वाला व्यक्ति।",
            "इस फ़ाइल में नेट-बैंकिंग पासवर्ड, UPI PIN, ATM PIN, OTP या CVV कभी नहीं होते।",
            "मृत्यु या आपातकाल के बाद: पहले पहचान के काग़ज़, फिर बैंक नॉमिनी, फिर बीमा दावे (LIC / स्वास्थ्य), फिर EPF / UAN।"
        ) else listOf(
            "Print this dossier on A4 paper. Keep one sealed copy with a trusted family member and one in a bank locker.",
            "Each table lists where originals live, the institution, optional account or policy numbers, nominee status, and the person who can help.",
            "This file never contains net-banking passwords, UPI PINs, ATM PINs, OTPs or CVVs. Those must stay out of this dossier.",
            "After a death or emergency, start with identity papers, then bank nominees, then insurance claims (LIC / health), then EPF / UAN."
        )
    val pdfReadinessByCategory get() = pick("Readiness by category", "श्रेणी के अनुसार तैयारी")
    val pdfConfidential get() = pick(
        "Confidential family copy. Generated on this phone by Kutumb Care. Nothing in this PDF was uploaded anywhere — the app has no internet permission.",
        "गोपनीय पारिवारिक प्रति। कुटुम्ब केयर द्वारा इस फ़ोन पर बनाई गई। इस PDF में कुछ भी कहीं अपलोड नहीं हुआ — ऐप के पास इंटरनेट अनुमति नहीं है।"
    )
    val pdfNotRecorded get() = pick("Not recorded yet", "अभी दर्ज नहीं")
    val pdfRecorded get() = pick("Recorded", "दर्ज किया गया")
    val pdfNotYet get() = pick("Not yet recorded", "अभी दर्ज नहीं किया")
    val pdfInstitution get() = pick("Institution", "संस्था")
    val pdfReference get() = pick("Reference", "संदर्भ")
    val pdfLocation get() = pick("Location", "स्थान")
    val pdfContact get() = pick("Contact", "संपर्क")
    val pdfNotes get() = pick("Notes", "नोट")
    val pdfOwner get() = pick("Owner", "मालिक")
    val pdfContinued get() = pick("(CONTINUED)", "(आगे जारी)")
    val pdfPage get() = pick("Page", "पृष्ठ")
    val pdfOf get() = pick("of", "/")

    fun category(category: AssetCategory): String = when (category) {
        AssetCategory.IDENTITY_GOVT -> pick("Identity & civil", "पहचान और नागरिक दस्तावेज़")
        AssetCategory.BANKING_FUNDS -> pick("Banking & funds", "बैंक और धन")
        AssetCategory.INVESTMENTS -> pick("Investments", "निवेश")
        AssetCategory.INSURANCE -> pick("Insurance", "बीमा")
        AssetCategory.REAL_ESTATE_LOANS -> pick("Property & loans", "संपत्ति और ऋण")
        AssetCategory.DIGITAL_LEGACY -> pick("Digital online assets", "डिजिटल ऑनलाइन संपत्ति")
        AssetCategory.TRUSTED_CONTACTS -> pick("Trusted contacts", "भरोसेमंद संपर्क")
    }

    fun categorySubtitle(category: AssetCategory): String = when (category) {
        AssetCategory.IDENTITY_GOVT -> pick("Aadhaar, PAN, passport, will", "आधार, PAN, पासपोर्ट, वसीयत")
        AssetCategory.BANKING_FUNDS -> pick("Accounts, FDs, lockers, cards", "खाते, FD, लॉकर, कार्ड")
        AssetCategory.INVESTMENTS -> pick("Demat, EPF, PPF, NPS, gold", "डीमैट, EPF, PPF, NPS, सोना")
        AssetCategory.INSURANCE -> pick("Life, health, vehicle", "जीवन, स्वास्थ्य, वाहन")
        AssetCategory.REAL_ESTATE_LOANS -> pick("Deeds, khata, home loans", "काग़ज़ात, खाता, गृह ऋण")
        AssetCategory.DIGITAL_LEGACY -> pick("Gmail, Apple ID, ITR, password manager", "Gmail, Apple ID, ITR, पासवर्ड मैनेजर")
        AssetCategory.TRUSTED_CONTACTS -> pick("CA, lawyer, agent, family", "CA, वकील, एजेंट, परिवार")
    }

    fun nominee(status: NomineeStatus): String = when (status) {
        NomineeStatus.NOMINEE_VERIFIED -> pick("Nominee verified", "नॉमिनी सत्यापित")
        NomineeStatus.NEEDS_NOMINEE -> pick("Needs nominee", "नॉमिनी चाहिए")
        NomineeStatus.NOT_APPLICABLE -> pick("No nominee needed", "नॉमिनी की ज़रूरत नहीं")
    }

    companion object {
        fun of(language: AppLanguage) = UiStrings(language.isHindi)
    }
}

/** The strings for the language the family chose; provided once at the root. */
val LocalStrings = staticCompositionLocalOf { UiStrings(hindi = false) }
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.ENGLISH }
