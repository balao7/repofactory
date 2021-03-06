package com.guidovezzoni.architecture.repository;


import com.guidovezzoni.architecture.cacheddatasource.MemoryCacheDataSource;
import com.guidovezzoni.architecture.datasource.DataSource;
import com.guidovezzoni.model.TimeStampedData;
import io.reactivex.Maybe;
import io.reactivex.Single;
import org.jetbrains.annotations.NotNull;

/**
 * Repository Pattern, with one level of cache
 *
 * @param <M> data model
 * @param <P> parameters required for obtaining the appropriate data
 */
@SuppressWarnings("unused")
public class SingleLevelCacheRepository<M, P> extends BaseRepository<M, P> implements Repository<M, P> {
    @NotNull
    private final DataSource<M, P> networkDataSource;

    @NotNull
    private final MemoryCacheDataSource<M, P> cacheDataSource;

    public SingleLevelCacheRepository(@NotNull DataSource<M, P> networkDataSource, @NotNull MemoryCacheDataSource<M, P> cacheDataSource) {
        this.networkDataSource = networkDataSource;
        this.cacheDataSource = cacheDataSource;
    }

    @NotNull
    @Override
    public Single<M> get(P params) {
        return cacheDataSource.get(params)
                .switchIfEmpty(Maybe.defer(() -> networkDataSource.getAndUpdate(params, cacheDataSource)))
                .map(TimeStampedData::getModel)
                .toSingle();
    }

    @NotNull
    @Override
    public Single<M> getLatest(P params) {
        return networkDataSource.getAndUpdate(params, cacheDataSource)
                .map(TimeStampedData::getModel)
                .toSingle();
    }

    @SuppressWarnings("WeakerAccess")
    public void setCacheValidity(Long newCacheValidity) {
        cacheDataSource.setCacheValidity(newCacheValidity);
    }

    public void invalidateCache() {
        cacheDataSource.invalidateCache();
    }
}
