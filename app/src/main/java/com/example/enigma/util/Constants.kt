package com.example.enigma.util

class Constants {
    companion object {

        const val ADDRESS_SIZE = 32 // bytes
        const val KEY_SIZE = 2048 // bits !!
        const val CLIENT_CONNECTION_RETRY_COUNT = 3
        const val CLIENT_CONNECTION_RETRY_DELAY = 5

        // Room Database
        const val DATABASE_NAME = "enigma-database"
        const val CONTACTS_TABLE = "Contacts"
        const val MESSAGES_TABLE = "Messages"
        const val KEYS_TABLE = "Keys"
        const val GUARDS_TABLE = "Guards"
        const val VERTICES_TABLE = "Vertices"
        const val EDGES_TABLE = "Edges"
        const val GRAPH_PATHS_TABLE = "GraphPaths"
        const val GRAPH_VERSIONS_TABLE = "GraphVersions"

        // Screens
        const val CONTACTS_SCREEN = "contacts"
        const val CHAT_SCREEN = "chat/{chatId}"
        const val ADD_CONTACT_SCREEN = "addContact"

        // Screens Arguments
        const val CHAT_ARGUMENT_KEY = "chatId"

        const val SELECTED_CHAT_ID = "Selected-Chat-Id"

        const val SERVER_URL = "http://10.52.145.90:5000"

        const val ONION_ROUTING_ENDPOINT = "$SERVER_URL/OnionRouting"

        const val SERVER_ADDRESS = "server-address"

        // TODO: Keys for testing. To be removed!!
        const val PUBLIC_KEY =
"""-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA13pyOqfbBfDP1HrCyxMw
ORgQBEsIfkN3CF6zsSUbydUdKRvs5UvDCDptrQ3y+ThsI5yXwgQyCcaiXCn+5Yzf
oD8j6r72EmGPUzOTJs3ZBLe7Dcsk1cOH4wTTH2HMsJ/2BKcfb0o2SK5VCUr/2LE1
FQtj7eoA/7yfivxNwnRz+MsyX90pme1uoZsHDBFNijSuvVnERpXkgzCRqC6bwYl2
Adbv52GFYcMITM9okZclUHwXIOOAVGtQax7/XCNoCCOcFY2YWPex1+iSPMfyfg6C
uJxKtNdBZ4LL19uJSHjz18KJZEpUQ4lSHhiw8usLN4mb6cgx7tXJhr1sE5b9yDK3
ZQIDAQAB
-----END PUBLIC KEY-----"""

        const val PRIVATE_KEY =
"""-----BEGIN ENCRYPTED PRIVATE KEY-----
MIIFHDBOBgkqhkiG9w0BBQ0wQTApBgkqhkiG9w0BBQwwHAQIyhxEM5lFRTgCAggA
MAwGCCqGSIb3DQIJBQAwFAYIKoZIhvcNAwcECNuk2hLDDcqXBIIEyKxZLfd3Z2hf
+6XdNIkmEbN0vm1MWIYIVn4g1IiQ17QwKw2sCVfGksA0raF0wOk3cNiu9kYIcaaV
O0w+22FBExkGIP0Kx8WKIHcxrLlrzCs7azqFM0+m0aeGibZTNKLolOtlH/JvUJv9
i+inoPqiIyyalNeQZsKkGj2SBao+LvnelrRgIdZ4AFsVajgbgRVEakaj+ffXvfP7
sZqmaBB1NQLIv+eHZ3HDUKGAU39OrRrlrE4rf0MRmYoNx8FINTz9ATLXak8W6SF8
t5QfAXXLtcZemAiVjQMu8UCt0+TrT3P/qar1TomGqx3TjMdeVpKAhIBIqv4i7BcW
yek3zzHuwBJ5fn0X/4PfDTqsGwm3wh0zpBNAEJBlAEXtnx6/0kkeRUtWes9U9+Ue
/VkWbDhJOtwjHoee2OR4gk41pDR0Q4uJpAwtA71GSkCsFcSW5rzr4+Apb90nYkGC
UKLlgeVfPwEoBlvCgDypydO2L4XGpMyDK01K/U0qDX0EEh8+EQfNSfa4GsG+vVS2
+2CWRhWQseAJavGTHKsV7QTlpAYbfrchUYoQp8tCq1pXzaqX8YNfWHEX8lLF/+o2
rYaroBND1LBgF7P4NsHjoKyn79kMr+r0gZFav+OLlGpTFIgN/1TdjjP/kA5GFbbs
zeHqLuGioNhRkzG8uWdt3CnfAXd8b6TMtDHm6gM6vQBw9Oo34aMRaGMJ2A+0uvDZ
XoIK3hvm6gE5/Z91V9BQzISyV9vitPpwR3miz/DqJLIGX3U3WYcmZ4xVL+qjjeVi
upCUaPtLSpgYpDxyMGY7ZWykx5eQ8F9JU4rx1d4rMANCF7D1Blon+290Kg1zoFyO
QI3f+vhQD4qOBiQYJLboNkKBQfhOoZzmHLjMB2fORwBovd08zbDGn9BHKk1bd34x
12Bib+LfpruNCr29cyoQqr1K9yKpTSN8IeSrRITisgbUFr0RLQa6jPoJB7ZyVi5u
yFUbGy/a+ML34+/hJDPLOugFTiu8TCaiXrMWDDiU75IzAf+K2b4KTne910V0Al/s
hppX4PSEVF1cwGKCNY26JetLHTFmDLhJvenvHCYJodxy2XKYSAZCO7x4vYsFXbhY
rhTJIGonLmmk4ceSQdxXZ1rIrU2bFSQpyHGbuFJ9Cl6aYfrqgrQVklF7itz5LlyH
n/dQ/PQk5VNaxaXztYNDAq9RIdU5Cj86axogswl8mZIWHInLgMe8PLaHU8c2Ui2p
DXUpYL5NKlZ381TAffcwkNis/hwg0VQamL5jRlAytyL2Nf60Xe/uEn3VarsvWcgA
/ah3WIgZM+mKw/ipg1QHOFPlJeATCtPZ6kAj7wHop95AH9qmZ2RkmmmTa15UDLdh
VBsbXaIHgB95C1VVUx7tjZFbZaxM2ioKEsUSzPI+T0WtWy80+otu9tcKJ4pzfb5J
OI0WVHc9bNrH7V8NOdCYkWIf4eU6cQicwfiJgv96+zamVMhZoahIiWGSb+PXX+Cj
ZDg3AwToyLQF9r5kmH/1l48zJOBCEeRZgmCKRg6BoutwufhFtxBkBoGSmqlqsFRh
159oFu3lvySMApH2hsXFM7X1lSRer9DHCYx/taO/IiS8UBV5EKoRV0fknGILZOBA
sAjwH0LbWe1xhKWHRXhcvA==
-----END ENCRYPTED PRIVATE KEY-----"""

        const val HEX_ADDRESS = "a186a6fba0ff7570b116b3df639e3713fab0a21f1cf62fb616d84c19217c8023"

        const val PASSPHRASE = "12345678";

        const val SERVER_PUBLIC_KEY =
"""-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArLisc0sWoqkAHbzV3/xs
TygvnFPtdjDpyQCpR7UdXXghiz8zLmcSglJCrdjpyHA35btZHWrzbfC9/svhzAYX
MYy4AjrRtyuAlQBU4nhxiKrCIiJoGNnKRCvJeyhqINQn5hgbHxNzdlz8+GbZBAF+
l6BhFcaL3UBDoRFKpZFulnV4gj9vhOySa61r97ysRyLK2CYlzvu+reUCRQ6JPO9f
mYaCSfHDfrCFUdpBlBY1gtal7HmPaJkJIqyMGwzxBSy/N6bIrzd5EskaN3Iipnwg
58/oiJCTIqpF5PzrBh6YhAXnMZp3u8iqpDZH6aGpqOjbTkbVJiwQXMqd5do/P9YS
4QIDAQAB
-----END PUBLIC KEY-----"""

        const val FOREIGN_PUBLIC_KEY =
"""-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA9mZB83nnrzpFeSkkr8au
1rmcYJhpyikM+4jCdJ5FowumVQ8Rq3yaYVTuz4mFQVyM28BldwXpG3FiL/aM7FHC
0H2tbJ/d4sB579WeGBELiHDWVvM5DrOfj/7QroFNDA6gI7Vmvk6o0BBQ+LiIeWTr
ivTUTkjZWoTo30RluPEpQKOxRoCdk+DaZNw0FBTdWUngkV8FjHQl6ObWWfQ7f3+t
yxMFHD9vMDP+nGXmd4NsnsDMABZ4Yn0rgyW1CF9phB1zLdOgxW+JNjK6wLbCqIyK
GCfJFnZpaImguAjdjPG318nXARZ4PqH3LbrYzPPanHz8IVGWA4vt1BlDROGmx8vJ
dwIDAQAB
-----END PUBLIC KEY-----"""

        const val FOREIGN_HEX_ADDRESS =
            "cbff2e12fb1f752cb17185f080f2b40301165a1051531cc0614e495ee2620ef9"
    }
}
