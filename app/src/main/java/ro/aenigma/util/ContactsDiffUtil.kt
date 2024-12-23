package ro.aenigma.util

import ro.aenigma.data.database.ContactEntity

class ContactsDiffUtil(
    oldList: List<ContactEntity>,
    newList: List<ContactEntity>
) : BaseDiffUtil<ContactEntity>(oldList, newList)
