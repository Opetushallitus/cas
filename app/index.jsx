import 'babel-polyfill';
import React from 'react';
import ReactDOM from 'react-dom';
import Bacon from 'baconjs';
import App from './components/App';
import {initAppState, changeListeners} from './appState.js'

require('./main.scss')
const appState = initAppState();

appState.onValue((state) => {
  ReactDOM.render(<App state={state} controller={changeListeners()}/>, document.getElementById('app'));
});


// ReactDOM.render(<App />, document.getElementById('app'));

