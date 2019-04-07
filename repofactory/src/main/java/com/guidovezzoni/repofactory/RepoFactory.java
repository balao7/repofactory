package com.guidovezzoni.repofactory;

import com.guidovezzoni.architecture.cache.CacheHelper;
import com.guidovezzoni.architecture.datasource.CachedDataSource;
import com.guidovezzoni.architecture.datasource.DataSource;
import com.guidovezzoni.architecture.datasource.NetworkFuncDataSource;
import com.guidovezzoni.architecture.repository.NoCacheRepository;
import com.guidovezzoni.architecture.repository.Repository;
import com.guidovezzoni.architecture.repository.SingleLevelCacheRepository;
import io.reactivex.Single;
import io.reactivex.annotations.NonNull;
import kotlin.jvm.functions.Function1;
import retrofit2.Response;

@SuppressWarnings({"WeakerAccess", "unused"})
public class RepoFactory {
    protected <M, P> DataSource<M, P> createNetworkDataSource(@NonNull Function1<P, Single<Response<M>>> endPointGet) {
        return new NetworkFuncDataSource<>(endPointGet);
    }

    protected <M, P> CachedDataSource<M, P> createCachedSource() {
        return new CachedDataSource<>(new CacheHelper(System::currentTimeMillis));
    }

    public <M, P> Repository<M, P> createRepo(RepoType repoType, @NonNull Function1<P, Single<Response<M>>> endPointGet) {
        DataSource<M, P> networkSource = createNetworkDataSource(endPointGet);

        switch (repoType) {
            case NO_CACHE:
                return new NoCacheRepository<>(networkSource);
            case SINGLE_CACHE:
                CachedDataSource<M, P> cachedDataSource = createCachedSource();
                return new SingleLevelCacheRepository<>(networkSource, cachedDataSource);
            default:
                throw new IllegalArgumentException("repo type not defined");
        }
    }
}
