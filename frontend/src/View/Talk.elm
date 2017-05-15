module View.Talk exposing (view)

import Html exposing (Html, div, text, h2, h3, p, select, option, button)
import Html.Attributes exposing (class, value, selected)
import Html.Events exposing (onInput, onClick)
import Messages exposing (Msg(..))
import Model.Talk exposing (Talk)


view : Talk -> Html Msg
view talk =
    div [ class "talk" ]
        [ h2 [] [ text talk.title ]
        , div [ class "talk__metadata" ] [ text <| talk.format ++ " / " ++ talk.length ++ " minutes / " ++ talk.lang ]
        , p [] [ text talk.body ]
        , div []
            [ h3 [] [ text "Expected audience" ]
            , p [] [ text talk.audience ]
            , h3 [] [ text "Equipment" ]
            , p [] [ text talk.equipment ]
            , h3 [] [ text "Outline" ]
            , p [] [ text talk.outline ]
            , h3 [] [ text "Info to the program committee" ]
            , p [] [ text talk.infoToProgramCommittee ]
            , h3 [] [ text "Talk status" ]
            , p [] [ viewTalkStatus talk ]
            , button [ onClick <| UpdateTalk talk ] [ text "Save" ]
            ]
        ]


viewTalkStatus : Talk -> Html Msg
viewTalkStatus talk =
    select [ onInput <| UpdateTalkField << setState talk ] <|
        List.map
            (viewOption talk)
            [ "DRAFT", "SUBMITTED", "APPROVED", "REJECTED", "HISTORIC" ]


viewOption : Talk -> String -> Html Msg
viewOption talk state =
    option [ value state, selected <| talk.state == state ] [ text state ]


setState : Talk -> String -> Talk
setState talk state =
    { talk | state = state }
