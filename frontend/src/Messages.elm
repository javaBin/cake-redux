module Messages exposing (Msg(..))

import Model.Event exposing (Event)
import Http


type Msg
    = GetEvents
    | GotEvents (Result Http.Error (List Event))
