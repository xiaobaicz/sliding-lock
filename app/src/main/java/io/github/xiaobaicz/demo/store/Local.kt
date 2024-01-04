package io.github.xiaobaicz.demo.store

import vip.oicp.xiaobaicz.lib.store.mmkv.annotation.MMKVStore
import vip.oicp.xiaobaicz.lib.store.serialize.gson.annotation.GsonSerialize

@MMKVStore
@GsonSerialize
interface Local {

    var password: String?

}