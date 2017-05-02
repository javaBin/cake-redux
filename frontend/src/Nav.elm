module Nav exposing (..)

import Navigation
import UrlParser exposing (Parser, (</>), oneOf, map, s, string, parseHash, top)
import Model.Page exposing (Page(..))


toHash : Page -> String
toHash page =
    case page of
        EventsPage ->
            "#/"

        EventPage eventId ->
            "#/" ++ eventId

        TalkPage eventId talkId ->
            "#/" ++ eventId ++ "/" ++ talkId


hashParser : Navigation.Location -> Page
hashParser location =
    Maybe.withDefault EventsPage <| parseHash pageParser location


pageParser : Parser (Page -> a) a
pageParser =
    oneOf
        [ map EventsPage (top)
        , map EventPage (string)
        , map TalkPage (string </> string)
        ]
