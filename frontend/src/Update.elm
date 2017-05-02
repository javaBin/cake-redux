module Update exposing (update)

import Model exposing (Model)
import Messages exposing (Msg(..))
import Requests exposing (getEvents)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        GetEvents ->
            ( model, getEvents )

        GotEvents (Ok events) ->
            ( { model | events = events }, Cmd.none )

        GotEvents _ ->
            ( model, Cmd.none )
