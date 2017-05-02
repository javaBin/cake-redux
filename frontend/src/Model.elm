module Model exposing (Model)

import Model.Event exposing (Event)
import Model.Talk exposing (Talk)


type alias Model =
    { events : List Event
    , eventId : Maybe String
    , talks : List Talk
    , talk : Maybe Talk
    }
