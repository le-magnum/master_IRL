/*
 * Copyright 2017-2021 The Technical University of Denmark
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package irlclient;

import java.util.Arrays;

public class StateActionPair
{
    private int hash = 0;

    private State state;

    private Action action;

    public StateActionPair(State state, Action action)
    {
        this.state = state;
        this.action = action;
    }

    @Override
    public int hashCode()
    {
        if (this.hash == 0) {
            final int prime = 31;
            int result = 1;
            result = result * prime + this.action.hashCode();
            result = result * prime + this.state.hashCode();
            this.hash = result;
        }
        return this.hash;
    }

    public State getState()
    {
        return state;
    }

    public Action getAction()
    {
        return action;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            System.err.println("this was tiggered");
            return true;
        }
        if (!(o instanceof StateActionPair)) {
            System.err.println("this was tiggered!");
            return false;
        }

        StateActionPair other = (StateActionPair) o;
        if (this.state.hashCode() == other.state.hashCode()){
            System.err.println("this was tiggered!!");
            if (this.action == other.action){
                System.err.println("this was tiggered!!!");
                System.err.println(this.state.toString());
                System.err.println(other.state.toString());
                return true;
            }
        }
        return false;
    }
}

