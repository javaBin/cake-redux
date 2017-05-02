module Main exposing (main)

import Html exposing (Html, program, text, div)


type alias Model =
    { text : String
    }


init : ( Model, Cmd Msg )
init =
    ( Model "Hello, world", Cmd.none )


type Msg
    = Placeholder


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    ( model, Cmd.none )


view : Model -> Html Msg
view model =
    div [] [ text model.text ]


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.none


main : Program Never Model Msg
main =
    program
        { init = init
        , update = update
        , view = view
        , subscriptions = subscriptions
        }
