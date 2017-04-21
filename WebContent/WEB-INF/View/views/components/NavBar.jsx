import React from 'react';
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import Drawer from 'material-ui/Drawer';
import MenuItem from 'material-ui/MenuItem';
import darkBaseTheme from 'material-ui/styles/baseThemes/darkBaseTheme';
import getMuiTheme from 'material-ui/styles/getMuiTheme';
import AppBar from 'material-ui/AppBar';
import Divider from 'material-ui/Divider';
import RaisedButton from 'material-ui/RaisedButton';
import DropDownMenu from 'material-ui/DropDownMenu';
import { white, red500, black, green } from 'material-ui/styles/colors';
import ActionPerson from 'material-ui/svg-icons/action/account-circle';
import ActionCheck from 'material-ui/svg-icons/action/check-circle';
import IconButton from 'material-ui/IconButton';
import IconMenu from 'material-ui/IconMenu';
import { browserHistory } from 'react-router';
import Avatar from 'material-ui/Avatar';

const lecturelist = [];
const exerciseslist = [];
var data;
var courseJSON;
var currentExerciseJSON;

const personStyles = {
    marginRight: 24,
    marginTop: 12,
};

const iconStyles = {
    marginRight: 12,
};

class NavBar extends React.Component {
    constructor() {
        super();
        this.state = {
            open: true,
            value: 1,
            dropdown: 0
        };
        this.handleChange = this.handleChange.bind(this);
        this.handleClickOnMenu = this.handleClickOnMenu.bind(this);
    }

    componentDidMount() {
        $.ajax({
            url: "http://localhost:8181/course",
            ddataType: 'json',
			jsonp: 'callback',
            method: 'GET',
            xhrFields: {
                withCredentials: true
            },
            success: function (course) {
                courseJSON = course;
                if (lecturelist.length === 0) {
                    for (var i = 0; i < course.lecture.length; i++) {
                        lecturelist.push(<MenuItem value={i} key={i} primaryText={course.lectures[i].title} />);
                    }
                }
                if (exerciseslist.length === 0) {
                    for (var j = 0; j < data.lecture[0].exercises.length; j++) {
                        exerciseslist.push(<MenuItem value={j} key={j} primaryText={course.lectures[0].exercises[j].title} onClick={this.handleClickOnMenu} />);
                    }
                }
            }.bind(this),
            error: function (error) {

            }.bind(this)
        });
    }

    handleClickOnMenu(event){
        var exerciseID = '';
        //exerciseID auslesen!

         $.ajax({
            url: "http://localhost:8181/exercise",
            dataType: 'json',
            method: 'GET',
            data: {
                "exerciseID": exerciseID
            },
            success: function (currentExercise) {
                currentExerciseJSON = currentExercise;
                //Daten aus der Component müssen in Component Exercise 
            }.bind(this),
            error: function (error) {

            }.bind(this)
        });
    }


    handleChange(event, index, value) {
        var count = exerciseslist.length;
        for (var i = 0; i < count; i++) {
            exerciseslist.pop();
        }
        for (var j = 0; j < data.lecture[value].exercises.length; j++) {
            exerciseslist.push(<MenuItem value={j} key={j} primaryText={data.lecture[value].exercises[j].exercise_title} />);
        }

        this.setState({ dropdown: value });
    }

    onClick(e) {
        e.preventDefault();
        if (e.target.textContent == "Settings") {
            browserHistory.push('/user');
        }
        else browserHistory.push('/');
    }


    render() {
        return (
            <div style={{ 'margin': '0' }}>
                <MuiThemeProvider muiTheme={getMuiTheme()}>
                    <div>
                        <AppBar
                            title="Colorado"
                            style={{ 'position': 'fixed', 'backgroundColor': '#bd051f', 'opacity': '0.9' }}
                            iconElementLeft={<Avatar
                                src="images/colorado.jpg"
                                size={45}
                            />}
                            iconElementRight={
                                <IconMenu
                                    iconButtonElement={<IconButton>
                                        <ActionPerson style={personStyles} color={white} hoverColor={black} />
                                    </IconButton>}
                                    targetOrigin={{ horizontal: 'right', vertical: 'top' }}
                                    anchorOrigin={{ horizontal: 'right', vertical: 'top' }}
                                >
                                    <MenuItem primaryText="Settings" onClick={this.onClick} />
                                    <MenuItem primaryText="Sign out" onClick={this.onClick} />
                                </IconMenu>
                            }
                        />

                        <Drawer
                            id="drawer"
                            docked={true}
                            open={this.state.open}
                            onRequestChange={(open) => this.setState({ open })}
                            containerStyle={{ 'position': 'fixed', 'margin': '0', 'top': '64px', 'height': 'calc(100% - 64px)', 'backgroundColor': '#959595', 'background': '-webkit-linear-gradient(#bbbbbb, #959595)' }}
                        >
                            <DropDownMenu value={this.state.dropdown} onChange={this.handleChange} >
                                {lecturelist}
                            </DropDownMenu>
                            <Divider />
                            {exerciseslist}

                        </Drawer>
                    </div>
                </MuiThemeProvider>
            </div>
        );
    }
}

export default NavBar;