module Update exposing (update)

import Model exposing (Model)
import Model.Page exposing (Page(..))
import Messages exposing (Msg(..))
import Requests exposing (getEvents, getTalks)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        ChangePage page ->
            updatePage page model

        GetEvents ->
            ( model, getEvents )

        GotEvents (Ok events) ->
            ( { model | events = events }, Cmd.none )

        GotEvents _ ->
            ( model, Cmd.none )

        GetTalks id ->
            ( model, getTalks id )

        GotTalks (Ok talks) ->
            ( { model | talks = talks }, Cmd.none )

        GotTalks _ ->
            ( model, Cmd.none )


updatePage : Page -> Model -> ( Model, Cmd Msg )
updatePage page model =
    case page of
        EventsPage ->
            ( model, Cmd.none )

        EventPage id ->
            ( model, getTalks id )

        TalkPage _ _ ->
            ( model, Cmd.none )
