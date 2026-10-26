package com.puneetfsn.kutumbcare.data

object DefaultChecklist {

    val ITEMS: List<ChecklistItem> = listOf(
        item(
            AssetCategory.IDENTITY_GOVT, 10,
            "Aadhaar card",
            "Physical Aadhaar copies, linked mobile number note, and DigiLocker availability. Do not write OTP or Aadhaar OTP PIN here."
        ),
        item(
            AssetCategory.IDENTITY_GOVT, 20,
            "PAN card",
            "PAN number location and whether a scanned copy sits with the Aadhaar file."
        ),
        item(
            AssetCategory.IDENTITY_GOVT, 30,
            "Passport, voter ID & driving licence",
            "Passport number booklet, voter ID and DL — where the originals and photocopies are kept."
        ),
        item(
            AssetCategory.IDENTITY_GOVT, 40,
            "Will & executor",
            "Registered or unregistered will, executor name, and the advocate or custodian who holds a copy.",
            NomineeStatus.NOT_APPLICABLE
        ),
        item(
            AssetCategory.BANKING_FUNDS, 50,
            "Savings & current accounts",
            "Bank name, branch, account type and whether a nominee is registered. Record account numbers only if useful — never net-banking passwords or UPI PINs."
        ),
        item(
            AssetCategory.BANKING_FUNDS, 60,
            "Fixed deposits & recurring deposits",
            "FD / RD receipts, issuing bank, maturity notes and nominee status."
        ),
        item(
            AssetCategory.BANKING_FUNDS, 70,
            "Bank locker",
            "Locker number, branch, joint holder, and the physical location of the locker key."
        ),
        item(
            AssetCategory.BANKING_FUNDS, 80,
            "Credit cards",
            "Issuing bank and a reminder to check outstanding balances. Never store CVV, ATM PIN or OTP."
        ),
        item(
            AssetCategory.INVESTMENTS, 90,
            "Demat & mutual funds",
            "Zerodha, Groww, Angel One, CAMS or KFintech folios. Note DP ID / client ID and nominee status — never broker passwords."
        ),
        item(
            AssetCategory.INVESTMENTS, 100,
            "EPF / UAN",
            "Universal Account Number for EPF withdrawal and the EDLI life cover. Confirm nominee on the EPFO portal is current."
        ),
        item(
            AssetCategory.INVESTMENTS, 110,
            "PPF passbook",
            "Public Provident Fund account, bank or post office branch, passbook location and nominee."
        ),
        item(
            AssetCategory.INVESTMENTS, 120,
            "NPS / PRAN",
            "Permanent Retirement Account Number, POP or employer NPS, and nominee / family pension details."
        ),
        item(
            AssetCategory.INVESTMENTS, 130,
            "Physical gold & sovereign gold bonds",
            "SGB certificates, jeweller receipts, locker slips and where the physical gold is stored."
        ),
        item(
            AssetCategory.INSURANCE, 140,
            "Term life / LIC policies",
            "Insurer, policy number, sum assured, nominee and the agent who can start a claim."
        ),
        item(
            AssetCategory.INSURANCE, 150,
            "Health insurance / mediclaim",
            "Family floater or individual policy, TPA card, policy number and cashless hospital notes."
        ),
        item(
            AssetCategory.INSURANCE, 160,
            "Vehicle insurance & RC",
            "Registration certificates, insurance papers and where the spare key is kept."
        ),
        item(
            AssetCategory.REAL_ESTATE_LOANS, 170,
            "Property deeds & khata",
            "Original registry, encumbrance certificate, khata / patta / 7-12 extract and property tax receipts."
        ),
        item(
            AssetCategory.REAL_ESTATE_LOANS, 180,
            "Home loan & credit-shield cover",
            "Housing or car loan papers, lender, outstanding-check reminder, and whether a loan-linked life cover exists.",
            NomineeStatus.NEEDS_NOMINEE
        ),
        item(
            AssetCategory.DIGITAL_LEGACY, 190,
            "Phone unlock note location",
            "Where a trusted person can find the written screen-unlock note in an emergency. Do not type the PIN itself into this app.",
            NomineeStatus.NOT_APPLICABLE
        ),
        item(
            AssetCategory.DIGITAL_LEGACY, 200,
            "Primary recovery email",
            "Main Gmail / Apple ID used for banks and insurance. Note whether Google Inactive Account Manager or Apple Legacy Contact is set. Never store the password.",
            NomineeStatus.NOT_APPLICABLE
        ),
        item(
            AssetCategory.DIGITAL_LEGACY, 210,
            "DigiLocker note location",
            "Where the DigiLocker mobile / security note is kept — not the PIN or Aadhaar OTP.",
            NomineeStatus.NOT_APPLICABLE
        ),
        item(
            AssetCategory.TRUSTED_CONTACTS, 220,
            "Family CA / tax consultant",
            "Chartered accountant who knows the ITRs, Form 16s and investment proofs.",
            NomineeStatus.NOT_APPLICABLE
        ),
        item(
            AssetCategory.TRUSTED_CONTACTS, 230,
            "Family lawyer / advocate",
            "Advocate who drafted the will, power of attorney or property papers.",
            NomineeStatus.NOT_APPLICABLE
        ),
        item(
            AssetCategory.TRUSTED_CONTACTS, 240,
            "LIC / insurance agent",
            "The agent or relationship manager who can file a life or health claim.",
            NomineeStatus.NOT_APPLICABLE
        )
    )

    private fun item(
        category: AssetCategory,
        order: Int,
        title: String,
        description: String,
        nominee: NomineeStatus = if (category.requiresNominee) NomineeStatus.NEEDS_NOMINEE else NomineeStatus.NOT_APPLICABLE
    ) = ChecklistItem(
        category = category.name,
        title = title,
        description = description,
        nomineeStatus = nominee.name,
        sortOrder = order
    )
}
