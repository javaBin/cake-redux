module Update exposing (update, updatePage)

import Model exposing (Model)
import Model.Page exposing (Page(..))
import Model.Event exposing (sortEvents)
import Messages exposing (Msg(..))
import Requests exposing (getEvents, getTalks, getTalk, updateTalk)
import Auth exposing (reauthenticate)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        ChangePage page ->
            updatePage page model

        GetEvents ->
            ( model, getEvents model.appConfig.token )

        GotEvents (Ok events) ->
            ( { model | events = sortEvents events }, Cmd.none )

        GotEvents _ ->
            ( model, Cmd.none )

        GetTalks id ->
            ( model, getTalks id model.appConfig.token )

        GotTalks (Ok talks) ->
            ( { model | talks = talks }, Cmd.none )

        GotTalks _ ->
            ( model, Cmd.none )

        GetTalk id ->
            ( model, getTalk id model.appConfig.token )

        GotTalk (Ok talk) ->
            ( { model | talk = Just talk }, Cmd.none )

        GotTalk _ ->
            ( model, Cmd.none )

        UpdateTalkField talk ->
            ( { model | talk = Just talk }, Cmd.none )

        UpdateTalk talk ->
            ( model, updateTalk talk model.appConfig.token )

        TalkUpdated (Ok talk) ->
            ( { model | talk = Just talk }, Cmd.none )

        TalkUpdated (Err talk) ->
            ( model, Cmd.none )

        Reauthenticate ->
            ( model, reauthenticate () )


updatePage : Page -> Model -> ( Model, Cmd Msg )
updatePage page model =
    case page of
        EventsPage ->
            ( model, getEvents model.appConfig.token )

        EventPage id ->
            ( { model | eventId = Just id }, getTalks id model.appConfig.token )

        TalkPage eventId talkId ->
            ( { model | eventId = Just eventId }, getTalk talkId model.appConfig.token )
