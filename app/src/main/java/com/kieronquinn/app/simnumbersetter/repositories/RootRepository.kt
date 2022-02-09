package com.kieronquinn.app.simnumbersetter.repositories

import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface RootRepository {

    suspend fun isRooted(): Boolean
    suspend fun runRootCommand(command: String): Shell.Result

}

class RootRepositoryImpl: RootRepository {

    override suspend fun isRooted(): Boolean {
        return withContext(Dispatchers.IO) {
            Shell.rootAccess()
        }
    }

    override suspend fun runRootCommand(command: String): Shell.Result {
        return withContext(Dispatchers.IO){
            Shell.su(command).exec()
        }
    }

}