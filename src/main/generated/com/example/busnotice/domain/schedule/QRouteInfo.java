package com.example.busnotice.domain.schedule;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRouteInfo is a Querydsl query type for RouteInfo
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QRouteInfo extends BeanPath<RouteInfo> {

    private static final long serialVersionUID = -1206751037L;

    public static final QRouteInfo routeInfo = new QRouteInfo("routeInfo");

    public final ListPath<BusInfo, SimplePath<BusInfo>> busInfos = this.<BusInfo, SimplePath<BusInfo>>createList("busInfos", BusInfo.class, SimplePath.class, PathInits.DIRECT2);

    public final StringPath busStopName = createString("busStopName");

    public final StringPath nodeId = createString("nodeId");

    public final StringPath regionName = createString("regionName");

    public QRouteInfo(String variable) {
        super(RouteInfo.class, forVariable(variable));
    }

    public QRouteInfo(Path<? extends RouteInfo> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRouteInfo(PathMetadata metadata) {
        super(RouteInfo.class, metadata);
    }

}

