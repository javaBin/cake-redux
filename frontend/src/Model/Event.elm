module Model.Event exposing (Event, eventDecoder, eventsDecoder, sortEvents)

import Json.Decode exposing (Decoder, string, list)
import Json.Decode.Pipeline exposing (decode, required)


type alias Event =
    { ref : String
    , name : String
    , slug : String
    }


sortEvents : List Event -> List Event
sortEvents =
    List.sortWith <|
        \a b ->
            case compare a.name b.name of
                LT ->
                    GT

                EQ ->
                    EQ

                GT ->
                    LT


eventsDecoder : Decoder (List Event)
eventsDecoder =
    list eventDecoder


eventDecoder : Decoder Event
eventDecoder =
    decode Event
        |> required "ref" string
        |> required "name" string
        |> required "slug" string
