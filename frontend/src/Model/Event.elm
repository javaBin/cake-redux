module Model.Event exposing (Event, eventDecoder, eventsDecoder)

import Json.Decode exposing (Decoder, string, list)
import Json.Decode.Pipeline exposing (decode, required)


type alias Event =
    { ref : String
    , name : String
    , slug : String
    }


eventsDecoder : Decoder (List Event)
eventsDecoder =
    list eventDecoder


eventDecoder : Decoder Event
eventDecoder =
    decode Event
        |> required "ref" string
        |> required "name" string
        |> required "slug" string
