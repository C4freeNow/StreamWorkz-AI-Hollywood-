package com.example.data

import kotlinx.coroutines.flow.Flow

class JournalRepository(private val journalDao: JournalDao) {
    val allEntries: Flow<List<JournalEntry>> = journalDao.getAllEntries()

    suspend fun getEntryById(id: Int): JournalEntry? = journalDao.getEntryById(id)

    suspend fun getEntryByDate(date: String): JournalEntry? = journalDao.getEntryByDate(date)

    suspend fun insert(entry: JournalEntry): Long = journalDao.insertEntry(entry)

    suspend fun update(entry: JournalEntry) = journalDao.updateEntry(entry)

    suspend fun delete(id: Int) = journalDao.deleteEntry(id)
}
