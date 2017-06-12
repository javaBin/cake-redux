module Messages exposing (Msg(..))

import Model.Event exposing (Event)
import Model.Talk exposing (Talk)
import Model.Page exposing (Page)
import Http


type Msg
    = ChangePage Page
    | GetEvents
    | GotEvents (Result Http.Error (List Event))
    | GetTalks String
    | GotTalks (Result Http.Error (List Talk))
    | GetTalk String
    | GotTalk (Result Http.Error Talk)
    | UpdateTalkField Talk
    | UpdateTalk Talk
    | TalkUpdated (Result Http.Error Talk)
    | Reauthenticate
