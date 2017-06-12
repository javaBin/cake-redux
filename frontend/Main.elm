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


initialRequests : Page -> String -> List (Cmd Msg)
initialRequests page token =
    case page of
        EventsPage ->
            [ getEvents token ]

        EventPage eventId ->
            [ getEvents token, getTalks eventId token ]

        TalkPage eventId talkId ->
            [ getEvents token, getTalks eventId token, getTalk talkId token ]


init : Flags -> Location -> ( Model, Cmd Msg )
init flags location =
    let
        model =
            Model [] Nothing [] Nothing <| initAppConfig flags

        page =
            hashParser location

        ( updatedModel, _ ) =
            updatePage page model

        requests =
            initialRequests page model.appConfig.token
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
