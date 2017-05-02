module Update exposing (update, updatePage)

import Model exposing (Model)
import Model.Page exposing (Page(..))
import Messages exposing (Msg(..))
import Requests exposing (getEvents, getTalks)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        ChangePage page ->
            ( model, updatePage page )

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


updatePage : Page -> Cmd Msg
updatePage page =
    case page of
        EventsPage ->
            getEvents

        EventPage id ->
            getTalks id

        TalkPage _ _ ->
            Cmd.none
