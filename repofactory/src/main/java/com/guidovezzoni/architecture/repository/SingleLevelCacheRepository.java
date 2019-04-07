package com.guidovezzoni.architecture.repository;


import com.guidovezzoni.architecture.datasource.CachedDataSource;
import com.guidovezzoni.architecture.datasource.DataSource;
import com.guidovezzoni.model.TimeStampedData;
import io.reactivex.Single;
import org.jetbrains.annotations.NotNull;

/**
 * Repository Pattern, with one level of cache
 *
 * @param <M> data model
 * @param <P> parameters required for obtaining the appropriate data
 */
@SuppressWarnings("unused")
public class SingleLevelCacheRepository<M, P> implements Repository<M, P> {
    private final DataSource<M, P> networkDataSource;
    private final CachedDataSource<M, P> cacheDataSource;

    public SingleLevelCacheRepository(DataSource<M, P> networkDataSource, CachedDataSource<M, P> cacheDataSource) {
        this.networkDataSource = networkDataSource;
        this.cacheDataSource = cacheDataSource;
    }

    @NotNull
    @Override
    public Single<M> get(P params) {
        return cacheDataSource.get(params)
                .switchIfEmpty(networkDataSource.getAndUpdate(params, cacheDataSource))
                .map(TimeStampedData::getModel)
                .toSingle();
    }

    @NotNull
    @Override
    public Single<M> getLatest(P params) {
        return networkDataSource.get(params)
                .doOnSuccess(cacheDataSource::set)
                .map(TimeStampedData::getModel)
                .toSingle();
    }

    public void setCacheValidity(Long newCacheValidity) {
        cacheDataSource.setCacheValidity(newCacheValidity);
    }

    public void invalidateCache() {
        cacheDataSource.invalidateCache();
    }
}
