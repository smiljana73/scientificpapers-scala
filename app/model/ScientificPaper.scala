package model

case class ScientificPaper(
    number: String,
    identificationNumber: String,
    documentType: String,
    recordType: String,
    paperType: String,
    author: String,
    mentor: String,
    title: String,
    publicationLanguage: String,
    geographicalArea: String,
    year: String,
    publisher: String,
    address: String,
    description: String,
    scientificField: String,
    scientificDiscipline: String,
    keywords: String,
    note: String,
    excerpt: String,
    dateOfAcceptance: String,
    defenseDate: String,
    commission: Commission
)

case class Commission(chairman: String, member: String, mentor: String)
