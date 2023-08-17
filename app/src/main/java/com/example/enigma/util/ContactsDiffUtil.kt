package com.example.enigma.util

import com.example.enigma.data.database.ContactEntity

class ContactsDiffUtil(
    oldList: List<ContactEntity>,
    newList: List<ContactEntity>
) : BaseDiffUtil<ContactEntity>(oldList, newList)
