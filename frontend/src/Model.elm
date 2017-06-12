module Model exposing (Model, Flags, AppConfig, initAppConfig)

import Model.Event exposing (Event)
import Model.Talk exposing (Talk)


type alias Model =
    { events : List Event
    , eventId : Maybe String
    , talks : List Talk
    , talk : Maybe Talk
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
