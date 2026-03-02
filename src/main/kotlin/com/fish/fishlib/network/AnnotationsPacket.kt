package com.fish.fishlib.network

@Target(AnnotationTarget.CLASS)
annotation class FishNetworkPacket(val value: String)

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class PacketStreamCodec