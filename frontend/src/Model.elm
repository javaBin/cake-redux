module Model exposing (..)

import Model.Event exposing (Event)
import Model.Talk exposing (Talk)


type alias Model =
    { events : List Event
    , eventId : Maybe String
    , talks : List Talk
    , talk : Maybe Talk
    , appConfig : AppConfig
    , filterData : FilterData
    }


type alias Flags =
    { host : String
    , token : String
    }


type alias AppConfig =
    { host : String
    , token : String
    }


initAppConfig : Flags -> AppConfig
initAppConfig flags =
    AppConfig flags.host flags.token


type alias FilterData =
    { formats : List String
    , lengths : List String
    }


initFilterData =
    FilterData [ "presentation", "workshop", "lightning-talk" ] [ "60 min", "40 min", "20 min", "10 min" ]
