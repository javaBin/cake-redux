module Model exposing (Model)

import Model.Event exposing (Event)


type alias Model =
    { events : List Event
    }
