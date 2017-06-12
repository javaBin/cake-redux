module Main exposing (main)

import Model exposing (Model, Flags, AppConfig, initAppConfig)
import Model.Page exposing (Page(..))
import Messages exposing (Msg(..))
import Update exposing (update, updatePage)
import View exposing (view)
import Subscriptions exposing (subscriptions)
import Requests exposing (getEvents, getTalks, getTalk)
import Navigation exposing (Location, programWithFlags)
import Nav exposing (hashParser)


initialRequests : Page -> List (Cmd Msg)
initialRequests page =
    case page of
        EventsPage ->
            [ getEvents ]

        EventPage eventId ->
            [ getEvents, getTalks eventId ]

        TalkPage eventId talkId ->
            [ getEvents, getTalks eventId, getTalk talkId ]


init : Flags -> Location -> ( Model, Cmd Msg )
init flags location =
    let
        model =
            Model [] Nothing [] Nothing

        page =
            hashParser location

        ( updatedModel, _ ) =
            updatePage page model

        requests =
            initialRequests page
    in
        ( updatedModel, Cmd.batch requests )


main : Program Flags Model Msg
main =
    programWithFlags (ChangePage << hashParser)
        { init = init
        , update = update
        , view = view
        , subscriptions = subscriptions
        }
