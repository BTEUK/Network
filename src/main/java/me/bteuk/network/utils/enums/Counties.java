package me.bteuk.network.utils.enums;

import java.util.Comparator;

public enum Counties {

    GREATER_LONDON("Greater London", Regions.LONDON),
    CITY_OF_LONDON("City of London", Regions.LONDON),

    NORTHUMBERLAND("Northumberland", Regions.NORTH_EAST),
    TYNE_AND_WEAR("Tyne and Wear", Regions.NORTH_EAST),
    DURHAM("Durham", Regions.NORTH_EAST),

    CUMBRIA("Cumbria", Regions.NORTH_WEST),
    LANCASHIRE("Lancashire", Regions.NORTH_WEST),
    MERSEYSIDE("Merseyside", Regions.NORTH_WEST),
    CHESHIRE("Cheshire", Regions.NORTH_WEST),
    GREATER_MANCHESTER("Greater Manchester", Regions.NORTH_WEST),

    NORTH_YORKSHIRE("North Yorkshire", Regions.YORKSHIRE),
    SOUTH_YORKSHIRE("South Yorkshire", Regions.YORKSHIRE),
    WEST_YORKSHIRE("West Yorkshire", Regions.YORKSHIRE),
    EAST_RIDING_OF_YORKSHIRE("East Riding of Yorkshire", Regions.YORKSHIRE),

    DERBYSHIRE("Derbyshire", Regions.EAST_MIDLANDS),
    NOTTINGHAMSHIRE("Nottinghamshire", Regions.EAST_MIDLANDS),
    LEICESTERSHIRE("Leicestershire", Regions.EAST_MIDLANDS),
    NORTHAMPTONSHIRE("Northamptonshire", Regions.EAST_MIDLANDS),
    RUTLAND("Rutland", Regions.EAST_MIDLANDS),
    LINCOLNSHIRE("Lincolnshire", Regions.EAST_MIDLANDS),

    WEST_MIDLANDS("West Midlands", Regions.WEST_MIDLANDS),
    WARWICKSHIRE("Warwickshire", Regions.WEST_MIDLANDS),
    STAFFORDSHIRE("Staffordshire", Regions.WEST_MIDLANDS),
    SHROPSHIRE("Shropshire", Regions.WEST_MIDLANDS),
    WORCESTERSHIRE("Worcestershire", Regions.WEST_MIDLANDS),
    HEREFORDSHIRE("Herefordshire", Regions.WEST_MIDLANDS),

    WEST_SUSSEX("West Sussex", Regions.SOUTH_EAST),
    EAST_SUSSEX("East Sussex", Regions.SOUTH_EAST),
    HAMPSHIRE("Hampshire", Regions.SOUTH_EAST),
    SURREY("Surrey", Regions.SOUTH_EAST),
    KENT("Kent", Regions.SOUTH_EAST),
    OXFORDSHIRE("Oxfordshire", Regions.SOUTH_EAST),
    BERKSHIRE("Berkshire", Regions.SOUTH_EAST),
    BUCKINGHAMSHIRE("Buckinghamshire", Regions.SOUTH_EAST),
    ISLE_OF_WIGHT("Isle of Wight", Regions.SOUTH_EAST),

    ESSEX("Essex", Regions.EAST_OF_ENGLAND),
    CAMBRIDGESHIRE("Cambridgeshire", Regions.EAST_OF_ENGLAND),
    NORFOLK("Norfolk", Regions.EAST_OF_ENGLAND),
    SUFFOLK("Suffolk", Regions.EAST_OF_ENGLAND),
    BEDFORDSHIRE("Bedfordshire", Regions.EAST_OF_ENGLAND),
    HERTFORDSHIRE("Hertfordshire", Regions.EAST_OF_ENGLAND),

    BRISTOL("Bristol", Regions.SOUTH_WEST),
    DEVON("Devon", Regions.SOUTH_WEST),
    CORNWALL("Cornwall", Regions.SOUTH_WEST),
    DORSET("Dorset", Regions.SOUTH_WEST),
    SOMERSET("Somerset", Regions.SOUTH_WEST),
    WILTSHIRE("Wiltshire", Regions.SOUTH_WEST),
    GLOUCESTERSHIRE("Gloucestershire", Regions.SOUTH_WEST);

    public final String label;
    public final Regions region;

    Counties(String label, Regions region) {
        this.label = label;
        this.region = region;
    }

}

